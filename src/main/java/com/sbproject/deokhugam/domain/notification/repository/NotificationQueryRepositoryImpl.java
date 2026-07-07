package com.sbproject.deokhugam.domain.notification.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.sbproject.deokhugam.domain.notification.entity.Notification;
import com.sbproject.deokhugam.domain.notification.entity.QNotification;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationQueryRepositoryImpl implements NotificationQueryRepository {

    private final JPAQueryFactory queryFactory;

    private static final QNotification n = QNotification.notification;

    @Override
    public Slice<Notification> findAllByUserId(
            UUID userId,
            UUID cursorId,
            Instant after,
            int limit
    ) {

        BooleanBuilder where = new BooleanBuilder();

        // 내 알림만 조회
        where.and(n.user.id.eq(userId));

        // Cursor Paging
        if (cursorId != null && after != null) {
            where.and(
                    n.createdAt.lt(after)
                            .or(
                                    n.createdAt.eq(after)
                                            .and(n.id.lt(cursorId))
                            )
            );
        }

        List<Notification> notifications = queryFactory
                .selectFrom(n)
                .where(where)
                .orderBy(
                        n.createdAt.desc(),
                        n.id.desc()
                )
                .limit(limit + 1)
                .fetch();

        boolean hasNext = notifications.size() > limit;

        if (hasNext) {
            notifications.remove(limit);
        }

        return new SliceImpl<>(
                notifications,
                PageRequest.of(0, limit),
                hasNext
        );
    }
}