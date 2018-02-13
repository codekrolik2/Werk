package org.werk.service;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class PageInfo {
	@Getter
	long itemsPerPage;
	@Getter
	long pageNumber;
}
