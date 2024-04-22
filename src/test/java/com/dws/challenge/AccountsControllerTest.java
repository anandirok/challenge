package com.dws.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import com.dws.challenge.domain.Account;
import com.dws.challenge.service.AccountsService;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@WebAppConfiguration
class AccountsControllerTest {

  private MockMvc mockMvc;

  @Autowired
  private AccountsService accountsService;

  @Autowired
  private WebApplicationContext webApplicationContext;

  @BeforeEach
  void prepareMockMvc() {
    this.mockMvc = webAppContextSetup(this.webApplicationContext).build();

    // Reset the existing accounts before each test.
    accountsService.getAccountsRepository().clearAccounts();
  }

  @Test
  void createAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1000");
  }

  @Test
  void createDuplicateAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());

    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\"}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNoBody() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON))
      .andExpect(status().isBadRequest());
  }

  @Test
  void createAccountNegativeBalance() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"Id-123\",\"balance\":-1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void createAccountEmptyAccountId() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
      .content("{\"accountId\":\"\",\"balance\":1000}")).andExpect(status().isBadRequest());
  }

  @Test
  void getAccount() throws Exception {
    String uniqueAccountId = "Id-" + System.currentTimeMillis();
    Account account = new Account(uniqueAccountId, new BigDecimal("123.45"));
    this.accountsService.createAccount(account);
    this.mockMvc.perform(get("/v1/accounts/" + uniqueAccountId))
      .andExpect(status().isOk())
      .andExpect(
        content().string("{\"accountId\":\"" + uniqueAccountId + "\",\"balance\":123.45}"));
  }
  // Positive case : Transfer money from account Id-124 to account Id123
  @Test
  void amountTransferPositive() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-124\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts/amount-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountTo" + "\":\"Id-123\",\"accountFrom\":\"Id-124\", \"transferAmount\": 1000}"))
            .andExpect(content()
                    .string("{\"transactionId\":\"5\",\"status\":\"" + "SUCCESS" + "\",\"message\":\"Successfully transfer the fund!\"}"));
  }

  @Test
  void amountTransferPositiveMultipleAccount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-124\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-125\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-126\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts/amount-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountTo" + "\":\"Id-123\",\"accountFrom\":\"Id-124\", \"transferAmount\": 100}"))
            .andExpect(content()
                    .string("{\"transactionId\":\"1\",\"status\":\"" + "SUCCESS" + "\",\"message\":\"Successfully transfer the fund!\"}"));
    this.mockMvc.perform(post("/v1/accounts/amount-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountTo" + "\":\"Id-123\",\"accountFrom\":\"Id-125\", \"transferAmount\": 200}"))
            .andExpect(content()
                    .string("{\"transactionId\":\"2\",\"status\":\"" + "SUCCESS" + "\",\"message\":\"Successfully transfer the fund!\"}"));
    this.mockMvc.perform(post("/v1/accounts/amount-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountTo" + "\":\"Id-123\",\"accountFrom\":\"Id-126\", \"transferAmount\": 200}"))
            .andExpect(content()
                    .string("{\"transactionId\":\"3\",\"status\":\"" + "SUCCESS" + "\",\"message\":\"Successfully transfer the fund!\"}"));
    this.mockMvc.perform(post("/v1/accounts/amount-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountTo" + "\":\"Id-123\",\"accountFrom\":\"Id-126\", \"transferAmount\": 400}"))
            .andExpect(content()
                    .string("{\"transactionId\":\"4\",\"status\":\"" + "SUCCESS" + "\",\"message\":\"Successfully transfer the fund!\"}"));


    Account account = accountsService.getAccount("Id-123");
    assertThat(account.getAccountId()).isEqualTo("Id-123");
    assertThat(account.getBalance()).isEqualByComparingTo("1900");
  }

  @Test
  void amountTransferNegative() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-124\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts/amount-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountTo" + "\":\"Id-123\",\"accountFrom\":\"Id-123\", \"transferAmount\": 1000}"))
            .andExpect(content()
                    .string("{\"code\":400,\"status\":\"BAD_REQUEST\",\"message\":\"Please check, To and From Account are the same!\"}"));
  }

  @Test
  void amountTransferNegativeZeroAmount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-124\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts/amount-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountTo" + "\":\"Id-123\",\"accountFrom\":\"Id-124\", \"transferAmount\": 0}"))
            .andExpect(content()
                    .string("{\"code\":400,\"status\":\"BAD_REQUEST\",\"message\":\"Transfer amount should be greater than zero!\"}"));
  }

  @Test
  void amountTransferNegativeAccountsInvalid() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-124\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts/amount-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountTo" + "\":\"Id-124\",\"accountFrom\":\"Id-123\", \"transferAmount\": 10000}"))
            .andExpect(content()
                    .string("{\"code\":400,\"status\":\"BAD_REQUEST\",\"message\":\"Insufficient amount in from account balance\"}"));
  }

  @Test
  void amountTransferNegativeAmount() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-124\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts/amount-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountTo" + "\":\"Id-124\",\"accountFrom\":\"Id-123\", \"transferAmount\": -10000}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  void amountTransferNegativeEmptyAccountTo() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-124\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts/amount-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountTo" + "\":\"\",\"accountFrom\":\"Id-123\", \"transferAmount\": 1000}"))
            .andExpect(status().isBadRequest());
  }

  @Test
  void amountTransferNegativeEmptyAccountFrom() throws Exception {
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-123\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountId\":\"Id-124\",\"balance\":1000}")).andExpect(status().isCreated());
    this.mockMvc.perform(post("/v1/accounts/amount-transfer").contentType(MediaType.APPLICATION_JSON)
            .content("{\"accountTo" + "\":\"Id-124\",\"accountFrom\":\"\", \"transferAmount\": 1000}"))
            .andExpect(status().isBadRequest());
  }

}
