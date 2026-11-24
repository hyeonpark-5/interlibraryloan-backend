package com.hyeon.interlibraryloan.book.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

	Optional<Item> findByLibraryIdAndBarcode(Long libraryId, String barcode);
}
