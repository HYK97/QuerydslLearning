package study.querydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.NoArgsConstructor;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.Entitiy.QMember.member;
import static study.querydsl.Entitiy.QTeam.team;


//명명 규칙 중요 Impl 이라고해야댐
public class MemberRepositoryImpl implements MemberRepositoryCustom {

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
}
