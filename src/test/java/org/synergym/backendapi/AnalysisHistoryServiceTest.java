package org.synergym.backendapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.synergym.backendapi.dto.AnalysisHistoryDTO;
import org.synergym.backendapi.entity.User;
import org.synergym.backendapi.repository.AnalysisHistoryRepository;
import org.synergym.backendapi.repository.UserRepository;
import org.synergym.backendapi.service.AnalysisHistoryService;

import java.util.List;

@SpringBootTest
public class AnalysisHistoryServiceTest {

    @Autowired
    private AnalysisHistoryService analysisHistoryService;

    @Autowired
    private AnalysisHistoryRepository analysisHistoryRepository;

    @Autowired
    private UserRepository userRepository;

    private User savedUser;

    @Test
    void createAnalysisHistory() {
        // savedUser = userRepository.save(User.builder()
        // .email("jy@test.com")
        // .name("정윤")
        // .password("pwpw")
        // .build());

        AnalysisHistoryDTO requestDto = AnalysisHistoryDTO.builder()
                .spineCurvScore(77)
                .spineScolScore(88)
                .pelvicScore(99)
                .neckScore(66)
                .shoulderScore(44)
                .frontImageUrl("front3.jpg")
                .sideImageUrl("side3.jpg")
                .build();

        AnalysisHistoryDTO resultDto = analysisHistoryService.createAnalysisHistory(requestDto, 6);

        System.out.println(resultDto);
    }

    @Test
    void getAnalysisHistoryById() {
        AnalysisHistoryDTO resultDto = analysisHistoryService.getAnalysisHistoryById(1);
        System.out.println(resultDto);
    }

    @Test
    void getAllHistoriesByUserId() {
        List<AnalysisHistoryDTO> results = analysisHistoryService.getAllAnalysisHistoryByUserId(6);
        System.out.println(results);
    }
}
