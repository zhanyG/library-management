package com.example.library.domain.service;

/**
 * 编号生成端口：借阅证号与馆藏条码（格式 CARD/ITEM + 年份 + 6 位序号）。
 */
public interface NumberGenerator {

    String nextCardNo();

    String nextBarcode();
}
