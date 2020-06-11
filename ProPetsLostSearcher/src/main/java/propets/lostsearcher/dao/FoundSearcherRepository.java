package propets.lostsearcher.dao;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import propets.lostsearcher.dto.FoundEntityDto;



public interface FoundSearcherRepository extends MongoRepository<FoundEntityDto, String>{
	
	List<FoundEntityDto> findByType(String type);

}
