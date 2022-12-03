package com.stock.recommend;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.annotation.Resource;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.data.stock.crawling.realtime.RealtimeComponent;
import com.data.stock.crawling.realtime.dto.RealtimePriceDto;
import com.search.result.SearchResultDto;
import com.search.viewCntRank.SearchViewCntRankMapper;
import com.user.info.UserInfoSessionDto;

@Service
public class StockRecommendService {

	@Resource // session에 저장해 둔 로그인 정보 가져오기
	private UserInfoSessionDto userInfoSessionDto;

	@Autowired
	private StockRecommendMapper stockRecommendMapper;

	@Autowired
	private RealtimeComponent realtimeComponent;
	
	@Autowired
	SearchViewCntRankMapper viewCntRankMapper;
	
	public Map<String, Object> getRecommendStock() {
		Map<String, Object> resultMap = new HashMap<>();
		Map<String, Object> contents = new HashMap<>();
		
		if (userInfoSessionDto.getUser_num() == null || userInfoSessionDto == null) {
			contents = getPopularStock();

		} else {
			String user_num = userInfoSessionDto.getUser_num();
			
			Set<String> stockSet = new HashSet<>(stockRecommendMapper.selectUserStockHolding(user_num));
			stockSet.addAll(stockRecommendMapper.selectUserWishlist(user_num));
			
			if (stockSet.size() < 3) {
				contents = getPopularStock();
				
			} else { // 보유 및 관심 주식 3개 이상
				int numberOfUsers = stockRecommendMapper.selectNumberOfUsers();
				
				if (numberOfUsers < 10) { // 사용자 10명 미만 - 관련 주식 중 랜덤
					List<String> stockList = new ArrayList<>(stockSet);
					contents = getRelatedStock(stockList);
				}
			}		
		}
		
		resultMap.put("response", "success_get_recommend_stock");
		resultMap.put("contents", contents);

		return resultMap;

	}

	private Map<String, Object> getPopularStock() {
		Map<String, Object> realtime = new HashMap<String, Object>();
		
		// 보유 및 관심 주식이 있는 사용자 수
		int userSize = stockRecommendMapper.selectUserHaveHoldingWish();
		List<SearchResultDto> searchResultDtoList;
		
		if (userSize < 10) {
			searchResultDtoList = viewCntRankMapper.selectViewCntTop5Stock();
		} else {
			searchResultDtoList = stockRecommendMapper.selectStockPopular();
		}
		Random random = new Random();
		
		int idx = random.nextInt(searchResultDtoList.size());
		SearchResultDto searchResultDto = searchResultDtoList.get(idx);
		
		String code = searchResultDto.getStock_code();
		
		RealtimePriceDto realtimePriceDto = realtimeComponent.getRealtimePrice(code);
		realtime.put("now", realtimePriceDto.getNow());
		realtime.put("diff", realtimePriceDto.getDiff());
		realtime.put("rate", realtimePriceDto.getRate());
		realtime.put("stock_code", code);
		realtime.put("name", searchResultDto.getCompany_name());
		
		return realtime;
	}

	// 관련 주식 중 랜덤으로 가져오기
	private Map<String, Object> getRelatedStock(List<String> stockList) {
		Map<String, Object> realtime = new HashMap<String, Object>();

		Random random = new Random();
		JSONParser parser = new JSONParser();

		try {
			Reader reader = new FileReader(".\\src\\main\\resources\\relations.json");
			JSONObject jsonObject = (JSONObject) parser.parse(reader);
			int num = 0;
			while (num < stockList.size()) {
				int idx = random.nextInt(stockList.size());
				String stock_code = stockList.get(idx);

				JSONArray jsonArray = (JSONArray) jsonObject.get(stock_code);

				num++;
				if (jsonArray.size() == 0) {
					continue;
				}
				JSONObject relation = (JSONObject) jsonArray.get(0);

				String code = (String) relation.get("code");

				RealtimePriceDto realtimePriceDto = realtimeComponent.getRealtimePrice(code);
				realtime.put("now", realtimePriceDto.getNow());
				realtime.put("diff", realtimePriceDto.getDiff());
				realtime.put("rate", realtimePriceDto.getRate());
				realtime.put("stock_code", code);
				realtime.put("name", relation.get("name"));

				break;
			}

		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return realtime;
	}

}
