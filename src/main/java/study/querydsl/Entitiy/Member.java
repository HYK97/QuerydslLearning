package study.querydsl.Entitiy;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;

import javax.persistence.*;

@Entity

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "username", "age"})
public class Member {


    @Id @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;
    private String username;
    private int age;

    @ManyToOne(fetch = FetchType.LAZY) //manyto One
    @JoinColumn(name = "TEAM_ID")
    private Team team;


    public Member(String username, int age, Team team) {

        this.username = username;
        this.age = age;
        if(team !=null){
            changeTeam(team);
        }

    }

    private void changeTeam(Team team) {
        this.team =team;
        team.getMembers().add(this);

    }

    public Member(String username, int age) {

        this(username, age, null);
    }


    public Member(String username) {
        this(username,0);
    }

}
