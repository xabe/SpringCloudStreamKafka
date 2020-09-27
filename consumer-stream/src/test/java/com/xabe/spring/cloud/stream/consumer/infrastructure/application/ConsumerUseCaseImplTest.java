package com.xabe.spring.cloud.stream.consumer.infrastructure.application;

import com.xabe.spring.cloud.stream.consumer.domain.entity.CarDO;
import com.xabe.spring.cloud.stream.consumer.domain.repository.ConsumerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConsumerUseCaseImplTest {

    private ConsumerRepository consumerRepository;

    private ConsumerUseCase consumerUseCase;

    @BeforeEach
    public void setUp() throws Exception {
        this.consumerRepository = mock(ConsumerRepository.class);
        this.consumerUseCase = new ConsumerUseCaseImpl(this.consumerRepository);
    }

    @Test
    public void shouldGetCars() throws Exception {
        //Given
        final CarDO carDO = CarDO.builder().build();
        when(this.consumerRepository.getCarDOS()).thenReturn(Collections.singletonList(carDO));

        //When
        final List<CarDO> result = this.consumerUseCase.getCars();

        //Then
        assertThat(result, is(notNullValue()));
        assertThat(result, is(hasSize(1)));
        assertThat(result, is(hasItem(carDO)));
    }

}