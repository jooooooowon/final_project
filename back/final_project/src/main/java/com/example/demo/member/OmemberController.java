package com.example.demo.member;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.auth.JwtTokenProvider;

@RestController
@CrossOrigin(origins="*")
@RequestMapping("/members")
public class OmemberController {
	@Autowired
	private OmemberService service;
	
	@Autowired
	private JwtTokenProvider tokenprovider;
	
	@Value("${spring.servlet.multipart.location}")
	private String path;
	
	//가입, 이미지업로드
	@PostMapping("")
	public Map join(OmemberDto dto, MultipartFile mf) {
		OmemberDto d = service.save(dto);
		Map map = new HashMap();
		boolean flag = true;
		try {
			File dir = new File(path+dto.getMemnum());
			dir.mkdir();
			
			String fname = mf.getOriginalFilename();
			String newpath = dir.getAbsolutePath()+"\\"+fname;
			File newFile = new File(newpath);
			mf.transferTo(newFile);
			
			dto.setImg(URLEncoder.encode(newpath, "utf-8"));
			OmemberDto result = service.save(dto);
			map.put("dto", result);
		}catch(Exception e) {
			e.printStackTrace();
			flag = false;
		}
		
		map.put("flag", flag);
		map.put("dto", d);
		return map;
	}
	
	//로그인
	@PostMapping("/login")
	public Map login(String id, String pwd) {
		Map map = new HashMap();
		boolean flag = false;
		OmemberDto dto = service.getById(id);
		if(dto!=null && pwd.equals(dto.getPwd())) {
			String token = tokenprovider.generateJwtToken(dto);
			flag = true;
			map.put("token", token);
		}
		map.put("flag", flag);
		return map;
	}
	
	//수정(닉네임, 비밀번호)
	@PutMapping("")
	public Map edit(OmemberDto dto, @RequestHeader(name = "token", required = false) String token) {
		boolean flag = true;
		Map map = new HashMap();
		if(token != null) {
			try {
				int memnum = tokenprovider.getMemnumFromToken(token);
				if(memnum != dto.getMemnum()) {
					flag = false;
				}
			}catch(Exception e) {
				flag = false;
			}
		}
		if(flag) {
			OmemberDto old = service.getByMemnum(dto.getMemnum());
			old.setPwd(dto.getPwd());
			old.setNickname(dto.getNickname());
			OmemberDto d = service.save(old);
			map.put("dto", d);
		}
		map.put("flag", flag);
		return map;
	}
	
	//탈퇴
	@DeleteMapping("/{memnum}")
	public Map del(@PathVariable("memnum") int memnum, @RequestHeader(name= "token", required = false) String token) {
		boolean flag = true;
		Map map = new HashMap();
		if (token != null) {
			try {
				int tempMemnum = tokenprovider.getMemnumFromToken(token);
				if(memnum != tempMemnum) {
					flag = false;
				}
			}	catch(Exception e) {
				flag =false;
			}
		}
		if(flag) {
			service.delMember(memnum);
		}
		map.put("flag",flag);
		return map;
	}
	
	//이미지파일 읽어오기
	@GetMapping("/imgs/{memnum}")
	public ResponseEntity<byte[]> readImg(@PathVariable("memnum") int memnum) {
		String fname = "";
		OmemberDto dto = service.getByMemnum(memnum);
		fname = dto.getImg();
		
		ResponseEntity<byte[]> result = null;
		try {
			if(fname != null && fname.length() != 0) {
				File f = new File(fname);
				HttpHeaders header = new HttpHeaders();
				header.add("Content-Type", Files.probeContentType(f.toPath()));
				result = new ResponseEntity<byte[]> (FileCopyUtils.copyToByteArray(f), header, HttpStatus.OK);
			}
		}catch(IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	

	// 내 정보 보기.
	@GetMapping("/{memnum}")
	public Map get(@PathVariable("memnum") int memnum, @RequestHeader(name= "token", required = false) String token) {
		Map map = new HashMap();
		boolean flag = true;
		try {
			OmemberDto dto = service.getByMemnum(memnum);
			map.put("dto", dto);
		}catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		map.put("flag", flag);
		return map;
	}
}
