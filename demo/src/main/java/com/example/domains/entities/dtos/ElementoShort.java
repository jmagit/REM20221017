package com.example.domains.entities.dtos;

import lombok.Value;

@Value
public class ElementoShort<K, V> {
	private K key;
	private V value;
}
