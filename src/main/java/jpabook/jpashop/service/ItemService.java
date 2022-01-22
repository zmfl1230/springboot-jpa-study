package jpabook.jpashop.service;

import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    final ItemRepository itemRepository;

    // 상품 생성 및 수정
    @Transactional
    public Long save(Item item) {
        itemRepository.save(item);
        return item.getId();
    }

    // 상품 조회
    public Item findById(Long id) {
        return itemRepository.findById(id);
    }


    // 상품 목록 조회
    public List<Item> findAll() {
        return itemRepository.findAll();
    }


}
