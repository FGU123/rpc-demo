package com.ex.demo.domain;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Order implements Serializable {
	
	private static final long serialVersionUID = 7811668951042395761L;

	private Long id;
	
	private Long buyerId;
	
	private Double amount;
}	
