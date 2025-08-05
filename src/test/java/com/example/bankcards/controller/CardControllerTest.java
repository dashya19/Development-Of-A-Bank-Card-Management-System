package com.example.bankcards.controller;

import com.example.bankcards.dto.CardDTO;
import com.example.bankcards.dto.TopUpDTO;
import com.example.bankcards.dto.TransferDTO;
import com.example.bankcards.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CardService cardService;

    @Test
    @WithMockUser
    void getUserCards_ShouldReturnCards() throws Exception {
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(1L);
        Page<CardDTO> page = new PageImpl<>(Collections.singletonList(cardDTO));
        when(cardService.getUserCards(any(), any())).thenReturn(page);

        mockMvc.perform(get("/api/cards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1));
    }

    @Test
    @WithMockUser
    void transferBetweenCards_ShouldReturnOk() throws Exception {
        doNothing().when(cardService).transferBetweenUserCards(any(), any());

        mockMvc.perform(post("/api/cards/transfer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fromCardId\":1,\"toCardId\":2,\"amount\":100}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    void topUpCard_ShouldReturnCard() throws Exception {
        CardDTO cardDTO = new CardDTO();
        cardDTO.setId(1L);
        when(cardService.topUpUserCard(any(), any())).thenReturn(cardDTO);

        mockMvc.perform(post("/api/cards/top-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"cardId\":1,\"amount\":100}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}