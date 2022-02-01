package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.Entitiy.Member;
import study.querydsl.Entitiy.QMember;
import study.querydsl.Entitiy.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;
import com.querydsl.jpa.JPAExpressions;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.Entitiy.QMember.member;
import static study.querydsl.Entitiy.QTeam.team;


@SpringBootTest
@Transactional
public class learningTest {


    @Autowired
    EntityManager em;


    JPAQueryFactory queryFactory;
    @Autowired
    MemberJpaRepository memberJpaRepository;

    @Test
    public void chktest() throws Exception{
        queryFactory =new JPAQueryFactory(em);
        queryFactory
                .delete(member)
                .execute();

        MemberTeamDto mtd =new MemberTeamDto(1L,"mm",1,1L,"tema1");
        boolean result = memberJpaRepository.ChkParam(mtd,new String[]{"memberId", "username", "age","teamId","teamName"});

        assertThat(result).isTrue();

    }





}
