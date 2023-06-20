package com.example.demo.community;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.likebnt.OlikebtnDto;
import com.example.demo.likebnt.OlikebtnService;
import com.example.demo.member.Omember;
import com.example.demo.member.OmemberDto;
import com.example.demo.member.OmemberService;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/ocommunity")
public class OcommunityController {
	@Autowired
	private OcommunityService service;
	
	@Autowired
	private OlikebtnService likeservice; //좋아요 누적
	
	@Value("${spring.servlet.multipart.location}")
	private String path; // C:/comm/

	// 게시글 전체목록 검색
	@GetMapping("")
	public Map getAll() {
		ArrayList<OcommunityDto> list = service.getAll();
		Map map = new HashMap();
		map.put("list", list);
		return map;
	}
	
	//게시글 번호로 검색
	@GetMapping("/{commnum}")
	public Map getByTag(@PathVariable("commnum") int commnum) {
		Map map = new HashMap<>();
		boolean flag = true;
		try {
			OcommunityDto dto = service.getByCommnum(commnum);
			map.put("dto", dto);
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		map.put("flag", flag);
		return map;
	}

	// 게시글 태그 검색
	@GetMapping("/{tag}")
	public Map getByTag(@PathVariable("tag") String tag) {
		Map map = new HashMap<>();
		boolean flag = true;
		try {
			ArrayList<OcommunityDto> tags = service.getByTag(tag);
			map.put("tags", tags);
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		map.put("flag", flag);
		return map;
	}

	// 게시글 생성.
	@PostMapping("")
	public Map save(OcommunityDto dto, MultipartFile[] mfArr) {
		Map map = new HashMap<>();
		boolean flag = true;
		try {
			// 게시글 이미지들이 들어가있을 폴더
			File dir = new File(path + dto.getCommnum());
			dir.mkdir();

			// 이미지들 폴더에 생성하기.
			String[] imgs = new String[3];
			for (int i = 0; i < 3; i++) {
				MultipartFile mf = mfArr[i];

				// 배열로 받은 파일들 중 없을 수도 있으니 조건문.
				if (mf != null && !mf.isEmpty()) {
					// 파일 삽입.
					String fname = mf.getOriginalFilename();
					String newpath = dir.getAbsolutePath() + "//" + fname;
					File newFile = new File(newpath);
					mf.transferTo(newFile);
					imgs[i] = newpath;
				}
			}

			// 삽입된 파일이 있다면 dto 업데이트 후 다시 저장.
			dto.setImg1(imgs[0]);
			dto.setImg2(imgs[1]);
			dto.setImg3(imgs[2]);
			service.save(dto);

		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		map.put("flag", flag);
		return map;
	}

	// 게시물 삭제하기 ( 신고당했을 때도 이용하기 )
	@DeleteMapping("")
	public Map delOcommunity(int num) {
		Map map = new HashMap<>();
		boolean flag = true;
		try {
			service.delOcommunity(num);
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}
		map.put("flag", flag);
		return map;
	}
	
	//좋아요 부분 수정
	@PatchMapping("/{btnlike}/{memnum}/{commnum}")
	public Map likeUpAndDown(@PathVariable("btnlike") int btnlike, @PathVariable("memnum") int memnum,
			@PathVariable("communm") int commnum) {
		boolean flag = true;
		OlikebtnDto dto = likeservice.getByMemnumAndCommnum(memnum, commnum);
		if(dto == null) { // 좋아요 안누름
			service.upBtn(btnlike, commnum);
			likeservice.save(dto);
		} else { // 좋아요 누름
			service.downBtn(btnlike, commnum);
			likeservice.delOlikebtn(dto.getLikebtn());
			flag = false; // 좋아요 눌려있으면 false 보내서 
		}
		Map map = new HashMap<>();
		map.put("flag", flag);
		return map;
	}
	
}