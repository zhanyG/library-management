package com.example.library.infrastructure.persistence;

import com.example.library.domain.service.NumberGenerator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TASK-003 编号生成测试：借阅证号 CARD+年份+6位序号、条码 ITEM+年份+6位序号。
 */
@SpringBootTest
class SequenceGeneratorAdapterTest {

    @Autowired
    private NumberGenerator numberGenerator;

    @Test
    @DisplayName("借阅证号格式为 CARD+年份+6位序号")
    void cardNoFormat() {
        assertThat(numberGenerator.nextCardNo()).matches("^CARD\\d{4}\\d{6}$");
    }

    @Test
    @DisplayName("连续生成的借阅证号不重复")
    void cardNoUnique() {
        String first = numberGenerator.nextCardNo();
        String second = numberGenerator.nextCardNo();

        assertThat(second).isNotEqualTo(first);
    }

    @Test
    @DisplayName("馆藏条码格式为 ITEM+年份+6位序号")
    void barcodeFormat() {
        assertThat(numberGenerator.nextBarcode()).matches("^ITEM\\d{4}\\d{6}$");
    }
}
