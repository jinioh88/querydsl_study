package study.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.domain.Member;
import study.querydsl.domain.QMember;
import study.querydsl.domain.QTeam;
import study.querydsl.domain.Team;
import study.querydsl.dto.MemberDTO;
import study.querydsl.dto.QMemberDTO;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.domain.QMember.member;
import static study.querydsl.domain.QTeam.team;

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

    @Test
    public void fetchTest() {
        QueryResults<Member> results = factory.selectFrom(member).fetchResults();
        System.out.println(results.getTotal());

        long count = factory.selectFrom(member).fetchCount();
    }

    @Test
    public void sort() {
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> members = factory.selectFrom(member).where(member.age.eq(100))
                .orderBy(member.age.desc(), member.username.asc().nullsLast()).fetch();

        assertThat(members.get(0).getUsername()).isEqualTo("member5");
    }

    @Test
    public void paging_test() {
        List<Member> members =
                factory.selectFrom(member).orderBy(member.username.desc()).offset(1).limit(2).fetch();

        assertThat(members.size()).isEqualTo(2);
    }

    @Test
    public void group_test() {
        List<Tuple> result = factory.select(team.name, member.age.avg()).from(member).join(member.team, team)
                .groupBy(team.name).fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamB.get(team.name)).isEqualTo("teamB");
    }

    @Test
    public void join() {
        List<Member> result
                = factory.selectFrom(member).join(member.team, team).where(team.name.eq("teamA")).fetch();

        assertThat(result).extracting("username").containsExactly("member1", "member2");
    }

    @Test
    public void thetaJoin() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result
                = factory.select(member).from(member, team).where(member.username.eq(team.name)).fetch();

        assertThat(result).extracting("username").containsExactly("teamA", "teamB");
    }

    @Test
    public void join_on_test() {
        List<Tuple> result
                = factory.select(member, team).from(member).leftJoin(member.team, team)
                .on(team.name.eq("teamA")).fetch();

        for(Tuple t : result) {
            System.out.println(t);
        }
    }

    @Test
    public void join_on_relation_test() {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        factory.select(member, team).from(member).leftJoin(team).on(member.username.eq("teamA")).fetch();
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void join_fetch_no() {
        em.flush();
        em.clear();

        Member member1 = factory.selectFrom(member).where(member.username.eq("member1")).fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("패치조인 안함").isFalse();
    }

    @Test
    public void join_fetch_on() {
        em.flush();
        em.clear();

        Member member1 = factory.selectFrom(member)
                .join(member.team, team).fetchJoin().where(member.username.eq("member1")).fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(member1.getTeam());
        assertThat(loaded).as("패치조인 안함").isTrue();
    }

    @Test
    public void subQuery_test() {
        QMember subMember = new QMember("subMember");

        List<Member> result = factory.selectFrom(member)
                .where(member.age.eq(JPAExpressions.select(subMember.age.max()).from(subMember))).fetch();

        assertThat(result).extracting("age").containsExactly(40);
    }

    @Test
    public void simpleProjection() {
        List<String> result = factory.select(member.username).from(member).fetch();
        for(String s : result) {
            System.out.println(s);
        }
    }

    @Test
    public void tupleProjection() {
        List<Tuple> result = factory.select(member.username, member.age).from(member).fetch();
        for(Tuple t : result) {
            String username = t.get(member.username);
            Integer age = t.get(member.age);
        }
    }

    @Test
    public void dtoProjection() {
        List<MemberDTO> result
                = factory.select(Projections.bean(MemberDTO.class, member.username, member.age)).from(member).fetch();
        for(MemberDTO dto : result) {
            System.out.println(dto);
        }
    }

    @Test
    public void dtoFieldProjection() {
        List<MemberDTO> result
                = factory.select(Projections.fields(MemberDTO.class, member.username, member.age)).from(member).fetch();
        for(MemberDTO dto : result) {
            System.out.println(dto);
        }
    }

    @Test
    public void testQueryProjection() {
        List<MemberDTO> result = factory.select(new QMemberDTO(member.username, member.age)).from(member).fetch();

        for(MemberDTO dto : result) {
            System.out.println(dto);
        }
    }

    @Test
    public void 동적쿼리_BooleanBuilder_test() {
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameParam, Integer ageParam) {
        BooleanBuilder builder = new BooleanBuilder();
        if (usernameParam != null) {
            builder.and(member.username.eq(usernameParam));
        }
        if (ageParam != null) {
            builder.and(member.age.eq(ageParam));
        }

        return factory.selectFrom(member).where(builder).fetch();
    }
}
