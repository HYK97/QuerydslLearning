package study.querydsl.Entitiy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@Commit
@SpringBootTest
class MemberTest {

    @Autowired
    EntityManager em;

    @Test
    public void testEntity() {


    }

}