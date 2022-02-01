package study.querydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;
import study.querydsl.Entitiy.Member;
import study.querydsl.Entitiy.Team;

@Data
@NoArgsConstructor
public class MemberTeamDto {

    private Long memberId;
    private String username;
    private int age;
    private Long teamId;
    private String teamName;


    @QueryProjection
    public MemberTeamDto(Long memberId, String username, int age, Long teamId, String teamName) {
        this.memberId = memberId;
        this.username = username;
        this.age = age;
        this.teamId = teamId;
        this.teamName = teamName;
    }

    public Team convertTeam() {
        return new Team(this.teamName);
    }

    public Member converMember() { // 좀더생각해봐야함
        return new Member(this.username,this.age);
    }
}
