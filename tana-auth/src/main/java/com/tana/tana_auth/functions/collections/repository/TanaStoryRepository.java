package com.tana.tana_auth.functions.collections.repository;

import com.tana.tana_common.model.TanaStory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TanaStoryRepository extends JpaRepository<TanaStory, Long> {
    List<TanaStory> findAllByOrderByDisplayOrderAscTanaStoryIdDesc();
    List<TanaStory> findAllByActiveTrueOrderByDisplayOrderAscTanaStoryIdDesc();
}
