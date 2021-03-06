package study.querydsl;


import com.querydsl.jpa.impl.JPAQueryFactory;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.Entitiy.Hello;
import study.querydsl.Entitiy.QHello;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.*;


@SpringBootTest
@Transactional
@Commit
class QuerydslApplicationTests {

	//@PersistenceContext
	@Autowired
	EntityManager em;

	@Test
	void contextLoads() {
		Hello hello =new Hello();
		em.persist(hello);

		JPAQueryFactory query =new JPAQueryFactory(em);
		QHello qHello =QHello.hello;

		Hello hello1 = query
				.selectFrom(qHello)
				.fetchOne();

		assertThat(hello1).isEqualTo(hello);
		assertThat(hello1.getId()).isEqualTo(hello.getId());

	}

}
