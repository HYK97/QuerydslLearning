package study.querydsl.repository;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.Entitiy.Member;
import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
class MemberJpaRepositoryTest {


    @Autowired
    EntityManager em;

    @Autowired
    MemberJpaRepository memberJpaRepository ;
    
    @Test
    public void basicTest() throws Exception{

        Member member = new Member("member1",5);

        memberJpaRepository.save(member);
        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);
        List<Member> result1 = memberJpaRepository.findAll();
        assertThat(result1).containsExactly(member);
        List<Member> result2 = memberJpaRepository.findByusername("member1");
        assertThat(result2).containsExactly(member);


    }


    @Test
    public void basicQueryDslTest() throws Exception{

        Member member = new Member("member1",5);
        memberJpaRepository.save(member);

        Member findMember = memberJpaRepository.findById(member.getId()).get();
        assertThat(findMember).isEqualTo(member);


        List<Member> result1 = memberJpaRepository.findAll_QueryDsl();
        assertThat(result1).containsExactly(member);

        List<Member> result2 = memberJpaRepository.findByusername_QueryDsl("member1");
        assertThat(result2).containsExactly(member);


    }

}