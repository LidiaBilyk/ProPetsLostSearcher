package propets.lostsearcher.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import propets.lostsearcher.dto.LostEntityDto;


public interface LostRepository extends MongoRepository<LostEntityDto, String>{

}
