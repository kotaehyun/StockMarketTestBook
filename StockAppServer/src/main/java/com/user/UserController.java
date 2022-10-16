package com.user;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("user")
public class UserController {
	
	@Resource
	private UserInfo userInfo;
	
	@Autowired
	UserService userService;
	
	@PostMapping("join") // 회원가입
	@ResponseBody
	public Map<String, Object> joinUser(@RequestParam Map<String, Object> map) {
		
		String id = map.get("id").toString();
		String password = map.get("password").toString();
		String password_confirm = map.get("password_confirm").toString();
		String simple_pwd_string = map.get("simple_pwd").toString();
		String name = map.get("name").toString();
		String nick_name = map.get("nick_name").toString();
		String email = map.get("email").toString();
		String phone_number = map.get("phone_number").toString();
		String address = map.get("address").toString();

		HashMap<String, Object> returnMap = new HashMap<>();
		
		String response = "";
		String sentence = "";
		
		// 필수 입력값(id, password, name, nick_name, email, phone_number) 중 하나라도 없는 경우
		if (id.equals("") || password.equals("") || name.equals("") || nick_name.equals("") || email.equals("") || phone_number.equals("")) {
			response = "failure_empty_some";
			sentence = "아이디, 비밀번호, 이름, 닉네임, 이메일, 전화번호를 모두 입력해 주세요.";
		}
		
		// id (1)5~20글자 영문자 + 숫자 (2)중복 확인 
		else if (!Pattern.matches("^(?=.*[a-zA-z])(?=.*[0-9])(?!.*[^a-zA-z0-9]).{5,20}$", id)) {
			response = "failure_wrong_format_id";
			sentence = "아이디는 영문자와 숫자를 하나 이상 포함해 5 ~ 20자로 입력해 주세요.";
		} else if (userService.findSameId(id) != null) {
			response = "failure_duplicate_id";
			sentence = "이미 존재하는 아이디입니다.";
		}
		
		// password (1)8~15글자, 영문자 + 숫자 + 특수문자 (2)password와 password_confirm 동일
		else if (!Pattern.matches("^(?=.*[a-zA-z])(?=.*[0-9])(?=.*[`~!@$!%*#^?&\\(\\)\\-_=+])(?!.*[^a-zA-z0-9`~!@$!%*#^?&\\(\\)\\-_=+]).{8,15}$", password)) {
			response = "failure_wrong_format_pwd";
			sentence = "비밀번호는 영문자, 숫자, 특수문자를 하나 이상 포함해 8~15자로 입력해 주세요.";
		} else if (!password.equals(password_confirm)) {
			response = "failure_different_pwd";
			sentence = "비밀번호 확인이 틀렸습니다.";
		}
		
		// simple_pwd 완료  (1)6자리, 숫자
		else if (!Pattern.matches("^[0-9]{6}$", simple_pwd_string)) {
			response = "failure_wrong_format_simple_pwd";
			sentence = "핀번호는 숫자 6자리로 입력해 주세요.";
		}
		
		// name (1)2~10글자, 문자
		else if (!Pattern.matches("^[가-힣|a-z|A-Z]{2,10}$", name)) {
			response = "failure_wrong_format_name";
			sentence = "이름은 문자 2 ~ 10자로 입력해 주세요.";
		}
		
		// nickname (1)1~8글자, 문자, 숫자 (2)중복 확인 
		else if (!Pattern.matches("^[ㄱ-ㅎ|가-힣|a-z|A-Z|0-9]{1,8}$", nick_name)) {
			response = "failure_wrong_format_nick_name";
			sentence = "닉네임은 한글, 영어, 숫자 1 ~ 8자리로 입력해 주세요.";
		} else if (userService.findSameNickName(nick_name) != null) {
			response = "failure_duplicate_nick_name";
			sentence = "이미 존재하는 닉네임입니다.";
		}
		
		// email (1)이메일 형식 (2)중복 확인 
		else if (!Pattern.matches("^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$", email)) {
			response = "failure_wrong_format_email";
			sentence = "이메일 형식에 맞춰 입력해 주세요.";
		} else if (userService.findSameEmail(email) != null) {
			response = "failure_duplicate_email";
			sentence = "이미 아이디가 존재하는 이메일입니다.";
		}
		
		// phone_number (1)10~11자리, 숫자 (2)중복 확인 
		else if (!Pattern.matches("^[0-9]{10,11}$", phone_number)) {
			response = "failure_wrong_format_phone_number";
			sentence = "전화번호는 01022223333처럼 숫자 11자리로 입력해 주세요.";
		} else if (userService.findSamePhoneNumber(phone_number) != null) {
			response = "failure_duplicate_phone_number";
			sentence = "이미 아이디가 존재하는 전화번호입니다.";
		}
		
		// address

		else {
			// simple_pwd 정수형으로 변형
			int simple_pwd = Integer.valueOf(simple_pwd_string);
			
			// user_num 생성
			String user_num = UUID.randomUUID().toString().replace("-", "");
			
			UserDTO userDTO = new UserDTO(user_num, id, password, simple_pwd, name, nick_name, email, phone_number, address);
			userService.joinUser(userDTO);
			
			response = "success_join";
			sentence = "회원가입이 완료됐습니다.";
		}

		returnMap.put("sentence", sentence);
		returnMap.put("response", response);
		return returnMap;
	}
	
	@PostMapping("login") // 로그인
	@ResponseBody
	public Map<String, Object> loginUser(@RequestParam Map<String, Object> map) {
		String id = map.get("id").toString();
		String password = map.get("password").toString();
		
		HashMap<String, Object> returnMap = new HashMap<>();
		
		String response = "";
		String sentence = "";
		
		if (id.equals("") && password.equals("")) {
			response = "failure_empty_id_pwd";
			sentence = "아이디와 비밀번호를 입력해 주세요.";
		} else if (id.equals("")) {
			response = "failure_empty_id";
			sentence = "아이디를 입력해 주세요.";
		} else if (password.equals("")) {
			response = "failure_empty_password";
			sentence = "비밀번호를 입력해 주세요.";
		} else {
			UserDTO userDTO = userService.loginUser(id);
			
			if (userDTO == null) {
				response = "failure_notExist_id";
				sentence = "존재하지 않는 아이디입니다.";
			} else if (!userDTO.getPassword().equals(password)) {
				response = "failure_wrong_password";
				sentence = "잘못된 비밀번호입니다.";
			} else {
				response = "success_login";
				sentence = "로그인이 완료됐습니다.";
				
				// 세션에 로그인 정보 ( id, user_num ) 저장
				userInfo.setId(userDTO.getId());
				userInfo.setUser_num(userDTO.getUser_num());	
			}
		}
		
		// API로 응답 넘겨주기
		returnMap.put("sentence", sentence);
		returnMap.put("response", response);
		return returnMap;
	}
	
	@GetMapping("session") // 로그인 정보
	@ResponseBody
	public UserInfo get() {
		return userInfo;
	}
	
	@PostMapping("logout") // 로그아웃
	public void logoutUser(@RequestParam Map<String, Object> map) {
		userInfo = new UserInfo();
	}
}