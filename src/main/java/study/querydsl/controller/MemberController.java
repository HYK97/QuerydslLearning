package study.querydsl.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import study.querydsl.Entitiy.Member;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;

import javax.transaction.Transactional;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;


    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) {
        return memberJpaRepository.searchByWhere(condition);
    }

    @Transactional
    @GetMapping("/v1/createMember")
    @ResponseBody
    public Object create(MemberTeamDto memberTeamDto) throws IllegalAccessException {

        memberJpaRepository.saveByQueryDsl(memberTeamDto);
        return memberJpaRepository.saveByQueryDsl(memberTeamDto)!=null ? memberJpaRepository.saveByQueryDsl(memberTeamDto): "실패";

    }

}
