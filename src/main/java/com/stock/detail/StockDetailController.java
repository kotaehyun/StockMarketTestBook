
package com.stock.detail;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.data.stock.crawling.news.NewsDto;

@RestController
@RequestMapping("stock")
public class StockDetailController {

	@Autowired
	StockDetailService stockDetailService;

	@GetMapping("info/{stock_code}")
	public Map<String, Object> stockDetailInfo(@PathVariable String stock_code) {
		return stockDetailService.stockDetailInfo(stock_code);
	}
	
	@GetMapping("realtime/{stock_code}")
	public Map<String, Object> stockDetailRealtime(@PathVariable String stock_code) {
		return stockDetailService.stockDetailRealtime(stock_code);
	}
	
	@GetMapping("graph/{stock_code}")
	public Map<String, Object> stockDetailGraph(@PathVariable String stock_code) {
		return stockDetailService.stockDetailGraph(stock_code);
	}
	
	@GetMapping("relations/{stock_code}")
	public Map<String, Object> stockDetailRelations(@PathVariable String stock_code) {
		return stockDetailService.stockDetailRelations(stock_code);
	}

	@GetMapping("news/{stock_code}")
	public List<NewsDto> stockDetailNews(@PathVariable String stock_code) {
		return stockDetailService.stockDetailNews(stock_code);
	}
	
	
}