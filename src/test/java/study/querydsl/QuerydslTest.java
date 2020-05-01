package study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.QMember;
import study.querydsl.domain.Team;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class QuerydslTest {
    @Autowired
    EntityManager em;

    JPAQueryFactory factory;

    @BeforeEach
    public void before() {
        factory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);        em.persist(member2);
        em.persist(member3);        em.persist(member4);
    }

    @Test
    public void querydsl_test() {
         QMember m = new QMember("m");

         Member findMember= factory.selectFrom(m).where(m.username.eq("member1")).fetchOne();

         assertThat(findMember.getUsername()).isEqualTo("member1");
    }
}
