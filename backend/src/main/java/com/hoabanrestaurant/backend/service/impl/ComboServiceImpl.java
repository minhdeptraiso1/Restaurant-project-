// service/impl/ComboServiceImpl.java
package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.CreateComboReq;
import com.hoabanrestaurant.backend.dto.response.ComboDto;
import com.hoabanrestaurant.backend.dto.response.DishDto;
import com.hoabanrestaurant.backend.dto.response.SuggestedMenuDto;
import com.hoabanrestaurant.backend.entity.Combo;
import com.hoabanrestaurant.backend.entity.ComboItem;
import com.hoabanrestaurant.backend.entity.Dish;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.enums.MenuStatus;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.mapper.ComboMapper;
import com.hoabanrestaurant.backend.mapper.DishMapper;
import com.hoabanrestaurant.backend.repository.ComboItemRepository;
import com.hoabanrestaurant.backend.repository.ComboRepository;
import com.hoabanrestaurant.backend.repository.DishRepository;
import com.hoabanrestaurant.backend.repository.OrderItemRepository;
import com.hoabanrestaurant.backend.service.ComboService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComboServiceImpl implements ComboService {
    private final ComboRepository comboRepo;
    private final ComboItemRepository itemRepo;
    private final DishRepository dishRepo;
    private final ComboMapper mapper;
    private final OrderItemRepository orderItemRepo;
    private final DishMapper dishMapper;

    @Override
    @Transactional
    public ComboDto create(CreateComboReq req) {
        if (comboRepo.existsByNameIgnoreCase(req.name()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Tên combo đã tồn tại");

        // Load & validate dishes
        List<UUID> dishIds = req.items().stream().map(CreateComboReq.ItemReq::dishId).toList();
        List<Dish> dishes = dishRepo.findAllById(dishIds);
        if (dishes.size() != dishIds.size())
            throw new BusinessException(ErrorCode.NOT_FOUND, "Có món trong combo không tồn tại");

        // Gộp quantity nếu user truyền trùng dishId
        Map<UUID, Integer> qtyMap = req.items().stream()
                .collect(Collectors.toMap(CreateComboReq.ItemReq::dishId, CreateComboReq.ItemReq::quantity, Integer::sum));

        // Tính suggestedSum = sum(price * qty)
        BigDecimal suggested = BigDecimal.ZERO;
        for (Dish d : dishes) {
            int q = qtyMap.get(d.getId());
            suggested = suggested.add(d.getPrice().multiply(BigDecimal.valueOf(q)));
            if (d.getStatus() != MenuStatus.ACTIVE)
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Món '" + d.getName() + "' đang không hoạt động");
        }

        Combo c = Combo.builder()
                .id(UUID.randomUUID())
                .name(req.name().trim())
                .description(req.description())
                .price(req.price())
                .imageUrl(req.imageUrl())
                .status(req.status() != null ? req.status() : MenuStatus.ACTIVE)
                .build();
        c = comboRepo.save(c);

        for (Dish d : dishes) {
            ComboItem link = ComboItem.builder()
                    .id(new ComboItem.Id(c.getId(), d.getId()))
                    .combo(c)
                    .dish(d)
                    .quantity(qtyMap.get(d.getId()))
                    .build();
            itemRepo.save(link);
        }

        // build dto
        return toDto(c, itemsOf(c.getId()), suggested);
    }

    @Override
    public ComboDto get(UUID comboId) {
        Combo c = comboRepo.findById(comboId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Combo không tồn tại"));
        var items = itemsOf(c.getId());
        var suggested = suggestedSum(items);
        return toDto(c, items, suggested);
    }

    @Override
    public List<ComboDto> listAll() {
        List<Combo> combos = comboRepo.findByStatus(MenuStatus.ACTIVE);
        List<ComboDto> out = new ArrayList<>();
        for (Combo c : combos) {
            var items = itemsOf(c.getId());
            out.add(toDto(c, items, suggestedSum(items)));
        }
        return out;
    }

    @Override
    @Transactional
    public ComboDto update(UUID comboId, CreateComboReq req) {
        Combo c = comboRepo.findById(comboId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Combo không tồn tại"));
        if (!c.getName().equalsIgnoreCase(req.name()) && comboRepo.existsByNameIgnoreCase(req.name()))
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Tên combo đã tồn tại");
        // Load & validate dishes
        List<UUID> dishIds = req.items().stream().map(CreateComboReq.ItemReq::dishId).toList();
        List<Dish> dishes = dishRepo.findAllById(dishIds);
        if (dishes.size() != dishIds.size())
            throw new BusinessException(ErrorCode.NOT_FOUND, "Có món trong combo không tồn tại");
        // Gộp quantity nếu user truyền trùng dishId
        Map<UUID, Integer> qtyMap = req.items().stream()
                .collect(Collectors.toMap(CreateComboReq.ItemReq::dishId, CreateComboReq.ItemReq::quantity, Integer::sum));
        // Tính suggestedSum = sum(price * qty)
        BigDecimal suggested = BigDecimal.ZERO;
        for (Dish d : dishes) {
            int q = qtyMap.get(d.getId());
            suggested = suggested.add(d.getPrice().multiply(BigDecimal.valueOf(q)));
            if (d.getStatus() != MenuStatus.ACTIVE)
                throw new BusinessException(ErrorCode.BAD_REQUEST, "Món '" + d.getName() + "' đang không hoạt động");
        }
        // Update combo
        c.setName(req.name().trim());
        c.setDescription(req.description());
        c.setPrice(req.price());
        c.setImageUrl(req.imageUrl());
        c.setStatus(req.status() != null ? req.status() : MenuStatus.ACTIVE);
        c = comboRepo.save(c);
        // Update combo items
        itemRepo.deleteAllByComboId(c.getId());
        for (Dish d : dishes) {
            ComboItem link = ComboItem.builder()
                    .id(new ComboItem.Id(c.getId(), d.getId()))
                    .combo(c)
                    .dish(d)
                    .quantity(qtyMap.get(d.getId()))
                    .build();
            itemRepo.save(link);
        }
        // build dto
        return toDto(c, itemsOf(c.getId()), suggested);
    }

    @Override
    @Transactional
    public void delete(UUID comboId) {
        Combo c = comboRepo.findById(comboId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Combo không tồn tại"));
        itemRepo.deleteAllByComboId(c.getId());
        comboRepo.delete(c);
    }


    @Override
    public List<Map<String, Object>> getAll() {
        return comboRepo.findByStatus(MenuStatus.ACTIVE).stream()
                .map(c -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", c.getId() != null ? c.getId().toString() : "");
                    m.put("name", c.getName() != null ? c.getName() : "");
                    m.put("price", c.getPrice() != null ? c.getPrice() : "");
                    return m;
                })
                .toList();
    }

    @Override
    public List<ComboDto> getAllbyAdmin() {
        List<Combo> combos = comboRepo.findAll();
        List<ComboDto> out = new ArrayList<>();
        for (Combo c : combos) {
            var items = itemsOf(c.getId());
            out.add(toDto(c, items, suggestedSum(items)));
        }
        return out;

    }


    // ===== Helpers =====
    private List<ComboDto.Item> itemsOf(UUID comboId) {
        return itemRepo.findByCombo_Id(comboId).stream()
                .map(ci -> new ComboDto.Item(ci.getDish().getId(), ci.getDish().getName(), ci.getQuantity()))
                .toList();
    }

    private ComboDto toDto(Combo c, List<ComboDto.Item> items, java.math.BigDecimal suggested) {
        var base = mapper.toDto(c);
        return new ComboDto(base.id(), base.name(), base.description(), base.price(), base.imageUrl(),
                base.status(), items, suggested);
    }

    private java.math.BigDecimal suggestedSum(List<ComboDto.Item> items) {
        if (items.isEmpty()) return java.math.BigDecimal.ZERO;
        Map<UUID, Integer> qty = items.stream().collect(Collectors.toMap(ComboDto.Item::dishId, ComboDto.Item::quantity));
        List<Dish> ds = dishRepo.findAllById(new ArrayList<>(qty.keySet()));
        java.math.BigDecimal s = java.math.BigDecimal.ZERO;
        for (Dish d : ds) s = s.add(d.getPrice().multiply(java.math.BigDecimal.valueOf(qty.get(d.getId()))));
        return s;
    }


    @Override
    public List<DishDto> getSuggestedDishesForUser(UUID userId) {
        int LIMIT = 4;

        List<DishDto> result = new ArrayList<>();

        // ===== 1) HISTORY =====
        var freqRows = orderItemRepo.findDishFrequencyByUser(userId);
        if (!freqRows.isEmpty()) {
            result.addAll(buildDishesFromFrequency(freqRows, LIMIT));
        }

        // Nếu đủ 4 thì return luôn
        if (result.size() >= LIMIT) return result.subList(0, LIMIT);

        // ===== 2) SIGNATURE =====
        var signatures = dishRepo.findTop4BySignatureTrueAndStatus(MenuStatus.ACTIVE);
        for (var d : signatures) {
            if (result.size() >= LIMIT) break;
            if (result.stream().noneMatch(x -> x.id().equals(d.getId()))) {
                result.add(dishMapper.toDto(d));
            }
        }

        if (result.size() >= LIMIT) return result.subList(0, LIMIT);

        // ===== 3) BEST SELLERS =====
        var best = orderItemRepo.getBestSellers();
        if (!best.isEmpty()) {
            var bestList = buildDishesFromFrequency(best, 20); // lấy nhiều để lấp chỗ trống
            for (var d : bestList) {
                if (result.size() >= LIMIT) break;
                if (result.stream().noneMatch(x -> x.id().equals(d.id()))) {
                    result.add(d);
                }
            }
        }

        if (result.size() >= LIMIT) return result.subList(0, LIMIT);

        // ===== 4) RANDOM =====
        Pageable p = PageRequest.of(0, LIMIT * 2);
        var randomDishes = dishRepo.findRandomActiveDishes(p);
        for (var d : randomDishes) {
            if (result.size() >= LIMIT) break;
            if (result.stream().noneMatch(x -> x.id().equals(d.getId()))) {
                result.add(dishMapper.toDto(d));
            }
        }

        return result.subList(0, Math.min(result.size(), LIMIT));
    }

    /**
     * Build DishDto từ danh sách tần suất
     */
    private List<DishDto> buildDishesFromFrequency(List<Object[]> rows, int limit) {
        List<UUID> ids = rows.stream()
                .map(r -> (UUID) r[0])
                .toList();

        var dishes = dishRepo.findAllById(ids);

        // giữ nguyên thứ tự theo freq
        dishes.sort(Comparator.comparingInt(d -> ids.indexOf(d.getId())));

        return dishes.stream()
                .filter(d -> d.getStatus() == MenuStatus.ACTIVE)
                .limit(limit)
                .map(dishMapper::toDto)
                .toList();
    }

    @Override
    public List<ComboDto> getSuggestedCombos() {
        Pageable limit = PageRequest.of(0, 4);
        var combos = comboRepo.findRandomCombos(limit);

        return combos.stream().map(c -> {
            var items = itemsOf(c.getId());
            var suggested = suggestedSum(items);
            var base = mapper.toDto(c);

            return new ComboDto(
                    base.id(),
                    base.name(),
                    base.description(),
                    base.price(),
                    base.imageUrl(),
                    base.status(),
                    items,
                    suggested
            );
        }).toList();
    }


    @Override
    public SuggestedMenuDto getSuggestedMenu(Jwt jwt) {
        UUID uid = UUID.fromString(jwt.getClaimAsString("uid"));
        var dishes = getSuggestedDishesForUser(uid);
        var combos = getSuggestedCombos();
        return new SuggestedMenuDto(dishes, combos);
    }


}
