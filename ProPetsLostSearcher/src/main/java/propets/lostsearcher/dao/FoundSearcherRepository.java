package propets.lostsearcher.dao;

import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import propets.lostsearcher.dto.FoundEntityDto;



public interface FoundSearcherRepository extends ElasticsearchRepository<FoundEntityDto, String>{
	
	List<FoundEntityDto> findByType(String type);

}
