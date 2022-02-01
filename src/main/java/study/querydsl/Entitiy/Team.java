package study.querydsl.Entitiy;


import lombok.*;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString(of = {"id", "name"})

public class Team {
    @Id @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;
    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> Members = new ArrayList<>();



    public Team(String name) {
        this.name = name;
    }


}
