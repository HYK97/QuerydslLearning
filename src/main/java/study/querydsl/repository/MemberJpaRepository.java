package study.querydsl.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;
import study.querydsl.Entitiy.Member;
import study.querydsl.Entitiy.Team;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.dto.QMemberTeamDto;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.StringUtils.hasText;
import static study.querydsl.Entitiy.QMember.member;
import static study.querydsl.Entitiy.QTeam.team;

@Repository
public class MemberJpaRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory;

    public MemberJpaRepository(EntityManager em ){
        this.em = em;
        this.queryFactory=new JPAQueryFactory(em); //queryFactory를 생성할때 2가지방법 지금과같이 생성자를 사용할지 아니면 bean으로 등록하여 실행할지 편한방식을 사용해서쓰자.
    }

    public void saveMember(Member member){
        em.persist(member);

    }

    public Optional<Member> findById(Long id){
        Member findMember =em.find(Member.class,id);
        return Optional.ofNullable(findMember);
    }

    public List<Member> findAll(){
        return em.createQuery("select m from Member m ",Member.class)
                .getResultList();
    }
    public List<Member> findAll_QueryDsl(){
        return queryFactory
                .selectFrom(member)
                .fetch();

    }

    public List<Member> findByusername(String username){
        return em.createQuery("select m from Member m where m.username=:username",Member.class)
                .setParameter("username",username)
                .getResultList();
    }

    public List<Team> findByTeamName(String teamName){
        return queryFactory
                .selectFrom(team)
                .where(team.name.eq(teamName))
                .fetch();
    }

    public List<Member> findByusername_QueryDsl(String username){
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

    public void saveTeam(Team team) {
        em.persist(team);
    }

    /**
     *
     * searchMember builder 이용
     *     */

    public List<MemberTeamDto> searchByBuilder(MemberSearchCondition condition){

        BooleanBuilder builder= new BooleanBuilder();

        if (hasText(condition.getUsername())) { //꿀팁 웹에서는 hasText가 null 이나 "" 를 확인해준다.
            builder.and(member.username.eq(condition.getUsername()));
        }
        if (hasText(condition.getTeamName())) {
            builder.and(team.name.eq(condition.getTeamName()));
        }
        if (condition.getAgeGoe() != null) {
            builder.and(member.age.goe(condition.getAgeGoe()));
        }
        if (condition.getAgeLoe() != null) {
            builder.and(member.age.loe(condition.getAgeLoe()));
        }

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
                .where(builder)
                .fetch();
    }




    /**
     *
     * searchMember WhereParam 이용
     *     */
    public List<MemberTeamDto> searchByWhere(MemberSearchCondition condition){

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
                        ,teamnameEq(condition.getTeamName())
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

    private BooleanExpression teamnameEq(String teamName) {
        return hasText(teamName)? team.name.eq(teamName) : null;
    }

    private BooleanExpression usernameEq(String username) {
        return hasText(username) ? member.username.eq(username) : null;
    }




    /**
     *
     * api create 그냥 만들어본거 service 단에 위치해야할것같음.
     * private Long memberId;
     *
     *     private String username;
     *     private int age;
     *     private Long teamId;
     *     private String teamName;
     *     */
    @Transactional
    public MemberTeamDto saveByQueryDsl(MemberTeamDto memberTeamDto) throws IllegalAccessException {

            if(ChkParam(memberTeamDto, new String[]{"username", "age","teamName"})) {
                Team team = null;
                Member member =null;

                if (findByTeamName(memberTeamDto.getTeamName()).size()==0) {
                    team =new Team(memberTeamDto.getTeamName());
                    saveTeam(team);
                }

                team = findByTeamName(memberTeamDto.getTeamName()).get(0);
                member= new Member(memberTeamDto.getUsername(), memberTeamDto.getAge(),team);
                saveMember(member);

                return new MemberTeamDto(member.getId(),member.getUsername(),member.getAge(),team.getId(),team.getName());

            }else {
                return null;
            }
    }


    //DTO 값 널체크
    public boolean ChkParam(Object obj, String[] valueNames) throws IllegalAccessException{

        for (Field field : obj.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            for (String str : valueNames) {
                if (field.getName().equals(str)) {
                    if (field.get(obj) == null || field.get(obj).toString().trim().equals("")) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


}
