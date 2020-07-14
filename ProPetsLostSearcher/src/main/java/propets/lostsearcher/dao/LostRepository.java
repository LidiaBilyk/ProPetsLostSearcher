package propets.lostsearcher.dao;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import propets.lostsearcher.dto.LostEntityDto;


public interface LostRepository extends ElasticsearchRepository<LostEntityDto, String>{

}
