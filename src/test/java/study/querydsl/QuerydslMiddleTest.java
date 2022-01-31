package study.querydsl;



import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
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
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.assertThat;
import static study.querydsl.Entitiy.QMember.member;
import static study.querydsl.Entitiy.QTeam.team;


@SpringBootTest
@Transactional
public class QuerydslMiddleTest {
    @Autowired
    EntityManager em;

    JPAQueryFactory queryFactory;



    @BeforeEach
    public void testEntity() {
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);

        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);


        em.flush();
        em.clear();

        List<Member> members = em.createQuery("select m from Member m", Member.class).getResultList();

        for (Member member : members) {
            System.out.println("member = " + member);
            System.out.println("member.getTeam() = " + member.getTeam());

        }

    }

    
    @Test
    public void simpleProjection() throws Exception{
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();
        for (String s : result) {
            System.out.println("s = " + s);
        }
    }
    @Test
    public void tupleProjection() throws Exception{
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();
        //중요 Tuple은 서비스나 상위 프레젠테이션 계층에 가지않도록 조심해야함 Repository or dao 에서만 사용하도록 해야한다.

        for (Tuple tuple : result) {
            String username =tuple.get(member.username);
            System.out.println("username = " + username);
            Integer age = tuple.get(member.age);
            System.out.println("age = " + age);
        }


    }


    @Test
    public void findDtoByJPQL() throws Exception{
        List<MemberDto> resultList = em.createQuery("select new study.querydsl.dto.MemberDto(m.username,m.age ) from Member m", MemberDto.class)
                .getResultList();// new operation을 이용해서 Dto로 가져오는방법 JPQL을 이용하지만 생성자만 이용해서 사용가능함

        for (MemberDto memberDto : resultList) {
            System.out.println("memberDto = " + memberDto);
        }

    }

    /**
     * queryDsl은
     * 생성자뿐만아니라 프로퍼티 필드에 직접주입도 가능하다
     * */


    @Test
    public void findDtoBySetter() throws Exception{ // 프로퍼티(setter) 를 활용한방법
        List<MemberDto> result = queryFactory
                .select(
                        Projections.bean(MemberDto.class
                                , member.username
                                , member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void findDtoByField() throws Exception{ // 필드를 활용한방법
        List<MemberDto> result = queryFactory
                .select(
                        Projections.fields(MemberDto.class
                                , member.username
                                , member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }



    @Test
    public void findDtoByConstructor() throws Exception{ // 생성자를 활용한방법
        List<MemberDto> result = queryFactory
                .select(
                        Projections.constructor(MemberDto.class
                                , member.username
                                , member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }


    @Test
    public void findUserDTo() throws Exception{ // 필드를 활용한방법
        List<UserDto> result = queryFactory
                .select(
                        Projections.fields(UserDto.class
                                , member.username.as("name") // Dto의 변수명과 다르면 as 를이용해 맞춰주도록 하자
                                , member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }

    @Test// 서브쿼리 별칭사용
    public void findByAliasUserDTo() throws Exception{ // 필드를 활용한방법

        QMember memberSub = new QMember("memberSub");
        List<UserDto> result = queryFactory
                .select(
                        Projections.fields(UserDto.class //필드는 이름보고
                                , member.username.as("name") // Dto의 변수명과 다르면 as 를이용해 맞춰주도록 하자

                                , ExpressionUtils.as(JPAExpressions
                                    .select(memberSub.age.max())
                                        .from(memberSub),"age")
                        ))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("userDto = " + userDto);
        }
    }


    @Test
    public void findUserDtoByConstructor() throws Exception{ // 생성자를 활용한방법
        List<UserDto> result = queryFactory
                .select(
                        Projections.constructor(UserDto.class // 생성자는 타입을 보고 들어간다.
                                , member.username
                                , member.age))
                .from(member)
                .fetch();

        for (UserDto userDto : result) {
            System.out.println("memberDto = " + userDto);
        }
    }


    @Test
    public void findDtoByQueryProjection() throws Exception{
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }

    }



    @Test

    public void dynamicQuery_BooleanBuilder() throws Exception{


        String usernameParam = "member1";
        Integer ageParam =10;

        List<Member> result = searchMember1(usernameParam,ageParam);
        assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameParam, Integer ageParam) {

        BooleanBuilder builder =new BooleanBuilder();
        if (usernameParam != null) {
            builder.and(member.username.eq(usernameParam));
        }
        if (ageParam != null) {
            builder.and(member.age.eq(ageParam));
        }

        return queryFactory
                .selectFrom(member)
                .where(builder)
                .fetch();
    }


}
