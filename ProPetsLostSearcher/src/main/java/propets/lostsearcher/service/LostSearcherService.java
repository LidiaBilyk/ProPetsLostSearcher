package propets.lostsearcher.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.support.MessageBuilder;
import propets.lostsearcher.dao.FoundSearcherRepository;
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
	FoundSearcherRepository foundSearcherRepository;
	

	@StreamListener(DispatcherService.INPUT)
	public void handler(LostEntityDto lostEntityDto) {
		if (lostEntityDto.getId().startsWith(MARK)) {
			String deleteId = lostEntityDto.getId().substring(MARK.length());			
			lostRepository.deleteById(deleteId);
			return;
		}
		lostRepository.save(lostEntityDto);
		List<FoundEntityDto> matches = foundSearcherRepository.findByType(lostEntityDto.getType());
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
