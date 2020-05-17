package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import study.querydsl.domain.Member;
import study.querydsl.domain.QMember;

import javax.persistence.EntityManager;
import java.util.List;

import static study.querydsl.domain.QMember.member;

@Repository
@RequiredArgsConstructor
public class MemberJpaRepository {
    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public void save(Member member) {
        em.persist(member);
    }

    public List<Member> findByUsername(String username) {
        return queryFactory.selectFrom(member)
                .where(member.username.eq(username)).fetch();
    }

    public List<Member> findAll() {
        return queryFactory.selectFrom(member).fetch();
    }
}
