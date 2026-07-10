package com.medtate.CalBag.notification.repository;

import com.medtate.CalBag.notification.domain.Notification;
import com.medtate.CalBag.notification.domain.NotificationStatus;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@SpringBootTest
@Transactional
class NotificationNPlusOneTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private EntityManager entityManager;

    private static final List<NotificationStatus> STATUSES =
            List.of(NotificationStatus.PENDING, NotificationStatus.SENT);
    private static final int WARMUP = 3;
    private static final int ROUNDS = 10;

    @Test
    @DisplayName("Fetch Join 적용 시 단일 쿼리로 조회한다")
    void withFetchJoin() {
        for (int i = 0; i < WARMUP; i++) {
            runWithFetchJoin();
        }

        long total = 0;
        for (int i = 0; i < ROUNDS; i++) {
            long start = System.nanoTime();
            runWithFetchJoin();
            total += System.nanoTime() - start;
        }

        System.out.println("=== Fetch Join 적용 ===");
        System.out.printf("평균 실행 시간: %.2fms%n", total / (double) ROUNDS / 1_000_000);
    }

    @Test
    @DisplayName("Fetch Join 미적용 시 N+1 쿼리가 발생한다")
    void withoutFetchJoin() {
        for (int i = 0; i < WARMUP; i++) {
            runWithoutFetchJoin();
        }

        long total = 0;
        for (int i = 0; i < ROUNDS; i++) {
            long start = System.nanoTime();
            runWithoutFetchJoin();
            total += System.nanoTime() - start;
        }

        System.out.println("=== Fetch Join 미적용 ===");
        System.out.printf("평균 실행 시간: %.2fms%n", total / (double) ROUNDS / 1_000_000);
    }

    private void runWithFetchJoin() {
        entityManager.clear();
        List<Notification> notifications =
                notificationRepository.findByUserIdAndStatusIn(1, STATUSES);
        notifications.forEach(n -> n.getEvent().getTitle());
    }

    private void runWithoutFetchJoin() {
        entityManager.clear();
        List<Notification> notifications = entityManager.createQuery(
                        "SELECT n FROM Notification n WHERE n.user.id = :userId AND n.status IN :statuses",
                        Notification.class)
                .setParameter("userId", 1)
                .setParameter("statuses", STATUSES)
                .getResultList();
        notifications.forEach(n -> n.getEvent().getTitle());
    }
}