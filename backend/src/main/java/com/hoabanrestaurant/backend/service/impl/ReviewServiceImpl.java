package com.hoabanrestaurant.backend.service.impl;

import com.hoabanrestaurant.backend.dto.request.CreateReviewReq;
import com.hoabanrestaurant.backend.dto.response.ReviewRes;
import com.hoabanrestaurant.backend.entity.Order;
import com.hoabanrestaurant.backend.entity.Review;
import com.hoabanrestaurant.backend.enums.ErrorCode;
import com.hoabanrestaurant.backend.enums.OrderStatus;
import com.hoabanrestaurant.backend.exception.BusinessException;
import com.hoabanrestaurant.backend.repository.OrderRepository;
import com.hoabanrestaurant.backend.repository.ReviewRepository;
import com.hoabanrestaurant.backend.service.ReviewService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepo;

    @Override
    public Review createReview(CreateReviewReq req, UUID userId) {

        // 1. Kiểm tra order có tồn tại không
        Order order = orderRepo.findById(req.orderId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Đơn hàng không tồn tại"));

        // 2. Kiểm tra đơn hàng thuộc người dùng hay không
        if (order.getUser() == null || !order.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN,
                    "Bạn không thể review đơn hàng không thuộc bạn");
        }

        // 3. Chỉ được review khi đơn đã thanh toán
        if (order.getStatus() != OrderStatus.PAID) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "Đơn hàng chưa thanh toán, không thể review");
        }

        // 4. Mỗi đơn hàng chỉ được review 1 lần
        if (reviewRepository.existsByOrder_Id(order.getId())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST,
                    "Đơn hàng đã được review trước đó");
        }

        // 5. Tạo review mới
        Review review = Review.builder()
                .order(order)
                .user(order.getUser())
                .rating(req.rating())
                .comment(req.comment())
                .build();

        return reviewRepository.save(review);
    }


    @Override
    public List<ReviewRes> getTop12Reviews() {
        Pageable pageable = PageRequest.of(0, 12);
        return reviewRepository.findLatestTop12(pageable)
                .stream()
                .map(this::toRes)
                .toList();
    }


    @Override
    public List<ReviewRes> getAllReviews() {
        return reviewRepository.findAll()
                .stream()
                .map(this::toRes)
                .toList();
    }

    @Override
    public void deleteReviewById(UUID id) {
        reviewRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Review không tồn tại"));

        reviewRepository.deleteById(id);
    }

    private ReviewRes toRes(Review review) {
        return new ReviewRes(
                review.getId(),
                review.getUser().getFullName(),   // lấy tên khách hàng từ User
                review.getRating(),
                review.getComment(),
                review.getCreatedAt(),
                review.getUser().getAvatarUrl() // lấy avatar từ User
        );
    }

}
