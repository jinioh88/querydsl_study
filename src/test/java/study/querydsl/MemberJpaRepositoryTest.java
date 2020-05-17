package study.querydsl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.querydsl.domain.Member;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberJpaRepositoryTest {
    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void test() {
        Member member = new Member("member1", 20);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findByUsername(member.getUsername()).get(0);
        assertThat(findMember).isEqualTo(member);

        List<Member> result = memberJpaRepository.findAll();
        assertThat(result).isEqualTo(member);
    }
}