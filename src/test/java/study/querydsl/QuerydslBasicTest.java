package study.querydsl;


import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.Entitiy.Member;

import static org.assertj.core.api.Assertions.*;
import static study.querydsl.Entitiy.QMember.member;
import static study.querydsl.Entitiy.QTeam.*;

import study.querydsl.Entitiy.QTeam;
import study.querydsl.Entitiy.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import java.util.List;


@SpringBootTest
@Transactional
public class QuerydslBasicTest {


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
    public void startJPQL() {
        Member singleResult = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        assertThat(singleResult.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQueryDsl() {

        Member findmember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        assertThat(findmember.getUsername()).isEqualTo("member1");
    }


    @Test
    public void search() {
        Member findmember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1").and(member.age.eq(10)))
                .fetchOne();

        assertThat(findmember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void searchAndParam() {
        Member findmember = queryFactory
                .selectFrom(member)
                .where(
                        member.username.eq("member1")
                        , (member.age.eq(10)))
                .fetchOne();

        assertThat(findmember.getUsername()).isEqualTo("member1");

    }


    @Test
    public void resultFetch() {
        //List
        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();
        //단 건
        Member findMember1 = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        //처음 한 건 조회
        Member findMember2 = queryFactory
                .selectFrom(member)
                .fetchFirst();
        //페이징에서 사용
        QueryResults<Member> results = queryFactory
                .selectFrom(member)
                .fetchResults();
        results.getTotal();
        List<Member> results1 = results.getResults();


        //count 쿼리로 변경
        long count = queryFactory
                .selectFrom(member)
                .fetchCount();
    }

    /**
     * 회원 정렬 순서
     * 1.회원 나이 내림차순
     * 2.회원 이름 올림차순
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     */
    @Test
    public void sort() {
        em.persist(new Member(null,100));
        em.persist(new Member("member5",100));
        em.persist(new Member("member6",100));
        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(
                        member.age.desc()
                        , member.username.asc().nullsLast()
                )
                .fetch();
        Member member5= result.get(0);
        Member member6= result.get(1);
        Member memberNull= result.get(2);
        assertThat(member5.getUsername()).isEqualTo("member5");
        assertThat(member6.getUsername()).isEqualTo("member6");
        assertThat(memberNull.getUsername()).isNull();


    }

    @Test
    public void paging1() {
        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)//몇번째 부터 끊어서 (N부터시작)
                .limit(2) //몇개씩 가져올까?
                .fetch();
        assertThat(result.size()).isEqualTo(2);
    }
    @Test
    public void paging2() {
        QueryResults<Member> queryResults = queryFactory.selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1)//몇번째 부터 끊어서 (N부터시작)
                .limit(2) //몇개씩 가져올까?
                .fetchResults();
        assertThat(queryResults.getTotal()).isEqualTo(4);
        assertThat(queryResults.getLimit()).isEqualTo(2);
        assertThat(queryResults.getOffset()).isEqualTo(1);
        assertThat(queryResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation() {
        List<Tuple> result = queryFactory
                .select(
                        member.count(),
                        member.age.sum(),
                        member.age.avg(),
                        member.age.max(),
                        member.age.min()
                )
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        //tuple은 get 하고 열의 이름을 그대로 쓰면된다.l
        assertThat(tuple.get(member.count())).isEqualTo(4);
        assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        assertThat(tuple.get(member.age.max())).isEqualTo(40);
        assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라*/
    @Test
    public void group() throws Exception{
        List<Tuple> result = queryFactory
                .select(
                        team.name,
                        member.age.avg()
                ).from(member)
                .join(member.team,team)
                .groupBy(team.name)
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);
        assertThat(teamA.get(team.name)).isEqualTo("teamA");
        assertThat(teamA.get(member.age.avg())).isEqualTo(15);


        assertThat(teamB.get(team.name)).isEqualTo("teamB");
        assertThat(teamB.get(member.age.avg())).isEqualTo(35);

    }

    /**
     * 팀 A 에 소속된 모든 회원
     * */
    @Test
    public void join() throws Exception{
        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team) //left right inner 조인은 member.team , team 과같이 연관관계가 있는것만 조인함
                .where(team.name.eq("teamA"))
                .fetch();


        assertThat(result.size()).isEqualTo(2);
        assertThat(result)
                .extracting("username") //해당 열이름
                .containsExactly("member1","member2");//


    }

    /**
     * 세타 조인(연관관계가 없는 필드로 조인)
     * 회원의 이름이 팀 이름과 같은 회원 조회
     */

    @Test
    public void theta_join() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team)  //select * from team, member where member.username = team.name
                .where(member.username.eq(team.name))
                .fetch();
        assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");

        assertThat(result.size()).isEqualTo(2);


    }

    /**
     * 예) 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두조회 */
    @Test
    public void join_on_filtering() throws Exception{
        List<Tuple> teamA = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team,team)
                .on(team.name.eq("teamA"))
                .fetch();

        for (Tuple tuple : teamA) {
            System.out.println("tuple = " + tuple);
        }

    }



    /**
     * 연관관계가 없는 엔티티 외부 조인
     * 회원의 이름이 팀 이름과 같은 대상 외부 조인*/
    @Test
    public void join_on_no_relation() throws Exception {
        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .join(team)
                .on(member.username.eq(team.name))
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }


    }
    /**
     * 페치조인이 없을때
     */


    @PersistenceUnit
    EntityManagerFactory emf;
    @Test
    public void fetchJoinNo () throws Exception{
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();
        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치조인미적용").isFalse();
    }

    @Test
    public void fetchJoinUse () throws Exception{
        em.flush();
        em.clear();

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team,team).fetchJoin()
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
        assertThat(loaded).as("페치조인적용").isTrue();
    }





}
