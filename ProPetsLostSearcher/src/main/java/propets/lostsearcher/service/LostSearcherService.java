package propets.lostsearcher.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.elasticsearch.common.unit.DistanceUnit;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.GeoDistanceQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.messaging.support.MessageBuilder;
import propets.lostsearcher.dao.LostRepository;
import propets.lostsearcher.dto.FoundEntityDto;
import propets.lostsearcher.dto.LostEntityDto;

@EnableBinding(DispatcherService.class)
public class LostSearcherService{
	
	private static final String MARK = "DELETE";
	
	@Autowired
	DispatcherService dispatcherService;
	@Autowired
	LostRepository lostRepository;
	@Autowired
	ElasticsearchRestTemplate template;
	

	@StreamListener(DispatcherService.INPUT)
	public void handler(LostEntityDto lostEntityDto) {
		if (lostEntityDto.getId().startsWith(MARK)) {
			String deleteId = lostEntityDto.getId().substring(MARK.length());			
			lostRepository.deleteById(deleteId);
			return;
		}
		lostRepository.save(lostEntityDto);
		BoolQueryBuilder builder = new BoolQueryBuilder()	
				.must(QueryBuilders.matchQuery("type", lostEntityDto.getType()))
				.filter(QueryBuilders.matchQuery("tags", lostEntityDto.getTags()).minimumShouldMatch("50%"));
		
		if (lostEntityDto.getSex() != null) {
			builder.filter(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("sex", lostEntityDto.getSex()))
			.should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("sex"))));
			
		}
		if (lostEntityDto.getBreed() != null) {
			builder.filter(QueryBuilders.boolQuery().should(QueryBuilders.matchQuery("breed", lostEntityDto.getBreed()).operator(Operator.AND))
			.should(QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("breed"))));
			
		}
		
		QueryBuilder geoBuilder = new GeoDistanceQueryBuilder("location")
				.point(lostEntityDto.getLocation().getLat(), lostEntityDto.getLocation().getLon()).distance(3, DistanceUnit.KILOMETERS);
		Query query = new NativeSearchQueryBuilder()				
				.withQuery(builder)
				.withFilter(geoBuilder)
				.build();

		List<FoundEntityDto> matches = template.search(query, FoundEntityDto.class).getSearchHits().stream()
				.map(h -> h.getContent()).collect(Collectors.toList());		
		if (!matches.isEmpty()) {
			sendMatches(lostEntityDto, matches);
		}		
	}

//	matches founds - one login, many ids
	public void sendMatches(LostEntityDto lostEntityDto, List<FoundEntityDto> matches) {
			Set<String> ids = matches.stream().map(e -> e.getId()).collect(Collectors.toSet());
			Map<String, Set<String>> result = new HashMap<>();
			result.put(lostEntityDto.getUserLogin(), ids);			
			dispatcherService.matches().send(MessageBuilder.withPayload(result).build());	
	}
}
