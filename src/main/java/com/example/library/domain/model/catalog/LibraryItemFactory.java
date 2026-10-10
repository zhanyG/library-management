package com.example.library.domain.model.catalog;

/**
 * 馆藏副本工厂：按借出物类型创建对应子类实例（Factory 模式）。
 */
public final class LibraryItemFactory {

    private LibraryItemFactory() {
    }

    public static LibraryItem create(ItemType itemType, String barcode, String location) {
        return switch (itemType) {
            case CHINESE_BOOK, FOREIGN_BOOK -> new Book(barcode, itemType, location, null, null, null);
            case CHINESE_MAGAZINE, FOREIGN_MAGAZINE -> new Magazine(barcode, itemType, location, null, null, null);
            case THESIS -> new Thesis(barcode, location, null, null, null);
        };
    }
}
