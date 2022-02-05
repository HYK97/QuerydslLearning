package study.querydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import study.querydsl.Entitiy.Member;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;

import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.Entitiy.QMember.member;
import static study.querydsl.Entitiy.QTeam.team;
import static org.springframework.data.domain.Pageable.*;

//명명 규칙 중요 Impl 이라고해야댐
public class MemberRepositoryImpl implements MemberRepositoryCustom  {



    private final JPAQueryFactory queryFactory;

    public MemberRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public List<MemberTeamDto> search(MemberSearchCondition condition){

        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId")
                        , member.username
                        , member.age
                        , team.id.as("teamId")
                        , team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername())
                        ,teamNameEq(condition.getTeamName())
                        ,ageGoeEq(condition.getAgeGoe())
                        ,ageLoeEq(condition.getAgeLoe()))
                .fetch();
    }


    private BooleanExpression ageLoeEq(Integer ageLoe) {
        return ageLoe!=null ? member.age.loe(ageLoe) : null;
    }

    private BooleanExpression ageGoeEq(Integer ageGoe) {
        return ageGoe!=null ? member.age.goe(ageGoe) : null;

    }
    private BooleanExpression teamNameEq(String teamName) {
        return hasText(teamName)? team.name.eq(teamName) : null;
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }


    // Paging Query---------------------------

    @Override //쉽거나 데이터가 적을때는이런식으로 하자.
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) { //몇페이지 조회할지 알려주는 매개변수

          QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId")
                        , member.username
                        , member.age
                        , team.id.as("teamId")
                        , team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername())
                        , teamNameEq(condition.getTeamName())
                        , ageGoeEq(condition.getAgeGoe())
                        , ageLoeEq(condition.getAgeLoe()))
                .offset(pageable.getOffset()) //오프셋 리미트 설정해줌
                .limit(pageable.getPageSize())
                .fetchResults();// 꼭 results 로 해야함.

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content,pageable,total);
    }


    //복잡한구현 두개 따로 하는경우 -> 카운트 쿼리가 컨텐츠 쿼리보다 쉬울때. 복잡한 쿼리 두방을 날리기보단 한개는 쉽게날리면 성능 최적화 시킬수있음
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) {

        List<MemberTeamDto> content = getMemberTeamDtos(condition, pageable);
          // 예를 들어 카운트 먼저날리고 없으면 안날릴때 와 같이 최적화함. 왠만하면 카운터 최적화하자 데이터 많을때
        JPAQuery<MemberTeamDto> countQuery = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId")
                        , member.username
                        , member.age
                        , team.id.as("teamId")
                        , team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername())
                        , teamNameEq(condition.getTeamName())
                        , ageGoeEq(condition.getAgeGoe())
                        , ageLoeEq(condition.getAgeLoe()));

        //카운트 쿼리 최적화 -> 마지막 페이지 혹은 첫번째 페이지에 페이징 수보다 컨텐츠가 적을때만 날아가도록 최적화 시킴.
        return PageableExecutionUtils.getPage(content,pageable,countQuery::fetchCount);
        //return new PageImpl<>(content,pageable,total);

    }

    //이런식으로 리팩토링해서써도된다.


    private List<MemberTeamDto> getMemberTeamDtos(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId")
                        , member.username
                        , member.age
                        , team.id.as("teamId")
                        , team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername())
                        , teamNameEq(condition.getTeamName())
                        , ageGoeEq(condition.getAgeGoe())
                        , ageLoeEq(condition.getAgeLoe()))
                .offset(pageable.getOffset()) //오프셋 리미트 설정해줌
                .limit(pageable.getPageSize())
                .fetch();// 꼭 results 로 해야함.
        return content;
    }

}
