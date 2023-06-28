package dbwls.springTransaction.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class MemberServiceTest {

    /**
     * 주석에 따라 각각의 Repository, Service 에서 @Transaction 조절 필요
     */
    @Autowired MemberService memberService;
    @Autowired MemberRepository memberRepository;
    @Autowired LogRepository logRepository;

    /**
     * memberService    @Transaction=OFF
     * memberRepository @Transaction=ON
     * logRepository    @Transaction=ON
     */
    @Test
    void outerTxOffSuccess() {
        // given
        String username = "outerTxOffSuccess";

        // when
        memberService.joinV1(username);

        // then: 모든 데이터가 정상 저장
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService    @Transaction=OFF
     * memberRepository @Transaction=ON
     * logRepository    @Transaction=ON Exception
     */
    @Test
    void outerTxOffFail() {
        // given
        String username = "로그예외_outerTxOffFail";

        // when
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // then: member 저장, log 롤백
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService    @Transaction=ON
     * memberRepository @Transaction=OFF
     * logRepository    @Transaction=OFF
     */
    @Test
    void singleTx() {
        // given
        String username = "singleTx";

        // when
        memberService.joinV1(username);

        // then: 모든 데이터가 정상 저장
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService    @Transaction=ON
     * memberRepository @Transaction=ON
     * logRepository    @Transaction=ON
     */
    @Test
    void outerTxOnSuccess() {
        // given
        String username = "outerTxOnSuccess";

        // when
        memberService.joinV1(username);

        // then: 모든 데이터가 정상 저장
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isPresent());
    }

    /**
     * memberService    @Transaction=ON
     * memberRepository @Transaction=ON
     * logRepository    @Transaction=ON Exception
     */
    @Test
    void outerTxOnFail() {
        // given
        String username = "로그예외_outerTxOnFail";

        // when
        assertThatThrownBy(() -> memberService.joinV1(username))
                .isInstanceOf(RuntimeException.class);

        // then: member 저장, log 롤백
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService    @Transaction=ON
     * memberRepository @Transaction=ON
     * logRepository    @Transaction=ON Exception
     */
    @Test
    void recoverExceptionFail() {
        // given
        String username = "로그예외_recoverExceptionFail";

        // when
        assertThatThrownBy(() -> memberService.joinV2(username))
                .isInstanceOf(UnexpectedRollbackException.class);

        // then: member 저장, log 롤백
        assertTrue(memberRepository.find(username).isEmpty());
        assertTrue(logRepository.find(username).isEmpty());
    }

    /**
     * memberService    @Transaction=ON
     * memberRepository @Transaction=ON
     * logRepository    @Transaction=ON(REQUIRES_NEW) Exception
     */
    @Test
    void recoverExceptionSuccess() {
        // given
        String username = "로그예외_recoverExceptionSuccess";

        // when
        memberService.joinV2(username);

        // then: member 저장, log 롤백
        assertTrue(memberRepository.find(username).isPresent());
        assertTrue(logRepository.find(username).isEmpty());
    }

}