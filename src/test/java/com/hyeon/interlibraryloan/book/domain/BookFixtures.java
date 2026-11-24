package com.hyeon.interlibraryloan.book.domain;

import com.hyeon.interlibraryloan.library.domain.Library;

/**
 * 책 도메인 테스트용 픽스처.
 */
public final class BookFixtures {

	private BookFixtures() {
	}

	public static BibRecord bibRecord(String isbn) {
		return BibRecord.builder()
				.isbn(Isbn.parse(isbn))
				.title("테스트 도서 " + isbn)
				.author("테스트 저자")
				.publisher("테스트 출판사")
				.publishedYear((short) 2020)
				.kdc("813.7")
				.build();
	}

	/** 대출 가능하고 상호대차가 허용된 자료 */
	public static Item availableItem(Library library, BibRecord bibRecord, MaterialType materialType, String barcode) {
		return Item.builder()
				.library(library)
				.bibRecord(bibRecord)
				.barcode(barcode)
				.callNumber("813.7-테54ㅌ")
				.shelfLocation("종합자료실")
				.materialType(materialType)
				.status(ItemStatus.AVAILABLE)
				.interlibraryLendable(true)
				.build();
	}
}
