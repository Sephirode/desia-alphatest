
# Desia_alpha_1.0 기획서 보완본(v0.2)

## I. 기본 게임 로직

### 1) 메인 메뉴

* 1. 게임 시작
* 2. 불러오기
* 3. 랭킹
* 4. 도움말
* 5. 종료 

### 2) 게임 시작: 캐릭터 선택 + 스토리 프레임

* 플레이어블 캐릭터 목록 → 설명/특수능력 표시 → 선택 확정
* 확정 직후 스토리 문단 출력(스토리 텍스트는 사용자가 작성, 코드는 “표시 프레임”만 제공) 

### 3) 전투 외 진행 기본 구조 = “이벤트-선택지-결과”

* 모든 진행은 아래 구조로 통일:

  * [이벤트 문장 출력] → [선택지] → [결과(스탯 변화 / 전투 / 상점 / 퀘스트 등)] 
* 전투가 아닐 때 활성 메뉴(허브 UI):

  * 1. 진행한다(랜덤 인카운터 호출)
  * 2. 스테이터스
  * 3. 인벤토리
  * 4. 장비
  * 5. 게임 저장(여기서 게임 종료도 가능) 
* “뒤로 가기” 선택지는 **모든 메뉴 깊이에서 항상 제공** 

---

## II. 데이터 주도 설계(핵심 원칙)

### 1) 하드코딩 금지 범위

* 스킬/적/소모품/장비/세트효과/특수효과는 전부 **외부 JSON**에서만 정의
* 코드는 “로더 + 검증기 + 실행 엔진”만 담당

### 2) 스킬은 ‘사용자’에 종속되지 않음

* “플레이어 전용/보스 전용 스킬” 같은 분류를 스킬 정의에 박지 않는다.
* 스킬은 **스킬 풀**이고, 실제 사용 가능 여부는 **개체(플레이어/적)가 어떤 스킬을 배정받았는지**로 결정.

### 3) 적은 ‘성장률 기반 템플릿’

* 적 JSON에는 “현재 레벨 스탯”이 아니라:

  * **1레벨 기준 스탯 + 레벨당 성장률(growth)** 을 가진다.
* 등장 시점 레벨에 맞춰 실스탯을 계산:

  * `stat(L) = base_stat + growth*(L - base_level)`

### 4) 스페셜 태그(특수효과) 분리

* “스탯만으로 구현이 어려운 효과”는 스킬/장비에 `special_tags`로 달고,
* 별도 레지스트리(예: specials.json)에서 실행 로직을 매핑한다.
* 목표: 특수공식 난립 방지 + 확장성 확보

---

## III. 전투 시스템

### 1) 전투 진행 페이즈

* 스탠바이 페이즈 → 배틀 페이즈 → 엔드 페이즈 

#### (1) 스탠바이 페이즈

* 각 엔티티 행동 선택:

  * 1. 기본 공격
  * 2. 스킬
  * 3. 도망
  * 4. 아이템 사용 
* 상태이상으로 행동 불발 가능:

  * 예: 마비/혼란 등(확률 기반) 
* 행동 처리 순서:

  * 스피드 내림차순
  * 동률이면: 레벨 > (현재 체력) 순으로 타이브레이크 

#### (2) 배틀 페이즈

* 선택된 명령을 순서대로 실행
* 피해 계산 및 “즉발형” 부가효과(상태이상 부여, 스탯 변경 등) 처리 
* 배틀 페이즈 도중

  * 플레이어 HP 0 → 게임 오버
  * 적 전멸 → 전투 종료

#### (3) 엔드 페이즈

* 도트형 상태이상(독/화상/출혈 등) 처리 
* 엔드 페이즈 결과로도

  * 플레이어 HP 0 → 게임 오버
  * 적 전멸 → 전투 종료

---

### 2) 명중률(스피드 기반) + 실명

* 기본 명중률:

  * 공격자SPD / 피격자SPD < 1 이면 그 값이 명중률
  * 1 이상이면 100% 명중
* 실명(Blind):

  * 명중률을 추가로 감소(최저치 clamp 적용)
* 설계 포인트:

  * “스피드”는 선턴뿐 아니라 **명중 안정성**까지 관여 → 스피드의 전략 가치 상승

---

### 3) 피해 타입 3종

* 물리(physical): 방어력 적용
* 마법(magic): 마법저항력 적용
* 고정(true): 방어/마저 무시하고 HP에 직격(실드는 예외)
* 한 스킬이 “물리+마법+고정”을 **복합으로 가질 수 있음**

---

### 4) 실드(보호막) 규칙(필수)

* 실드는 HP보다 먼저 깎인다.
* 실드는 **방어/마저로 경감되지 않는다**(=들어온 피해량 그대로 감소).
* 처리 순서(개념):

  1. 실드에서 먼저 차감
  2. 남은 피해가 있으면 HP 차감
* 실드는 스킬/장비 효과로 부여/재생 가능

---

### 5) 치명타율(crit)

* 치명타율은 “기본 공격/스킬”에 **+50% 추가 피해**를 부여할 확률 
* 추천 구현:

  * 치명타는 “최종 피해량”에 곱(예: ×1.5)으로 적용
  * 단, 고정(true)은 치명타 적용 여부를 옵션으로(기획 단계에서 스위치 가능)

---

### 6) 피해 감소 공식(현 기획서의 LoL식 공식은 ‘옵션화’)

`기획gpt.txt`에는 LoL식(100/(100+방어))이 들어가 있는데 , 너는 “체력 100 이하 ~ 최종 2000~4000” 스케일을 원했고 LoL식은 초반 체감이 어긋난다고 했었음.

**해결책(기획서 반영 방식):**

* 피해 감소는 “공식 1개 고정”이 아니라, `config.json`에서 선택 가능한 **감소 모델**로 둔다.

  * 모델 A(LoL식): `mult = 100/(100+def)`
  * 모델 B(선형 캡): `mult = clamp(1 - def * k, minMult, 1)`
  * 모델 C(완만한 곡선): `mult = 1/(1 + def*k)`
* 이렇게 해두면, 밸런싱 단계에서 수치 스케일 바뀌어도 공식만 교체/튜닝 가능.

---

### 7) 상태이상 + 등급 저항

* 상태이상 유형: 도트(화상/독/출혈), 행동 방해(빙결/마비/패닉), 명중 방해(실명) 
* 등급 기반 저항/면역:

  * 보스: 패닉 면역(확정)
  * 엘리트 이상: 패닉 확률 감소 등(확정)
* 지속/해제:

  * 일정 턴 후 자연 해제 또는 감소
  * 스킬/소모품으로 해제 가능 

---

### 8) 전투 보상(승리 시)

* 경험치: 처치한 적들의 “경험치 스탯 합”만큼 획득 
* 골드: 얻은 경험치의 80% 
* 드랍: 일정 확률로 적이 착용한 장비 일부 드랍 
* 레벨업:

  * 경험치 요구량을 초과분 처리(초과 경험치 이월)
  * 한번에 여러 레벨 상승 가능 

---

## IV. 스킬 시스템

### 1) 스킬 개념(요약)

* 공격 스킬: 물리/마법/복합/고정 피해를 가할 수 있음 
* 변화 스킬: 버프/디버프/유틸(회복, 실드 부여, 방어 감소, 다음 공격 강화 등) 

### 2) 스킬 수식: “항(term) 기반”이 기본

특수공식을 따로 만들지 말고, 아래처럼 “항 조합”으로 대부분 표현:

* 상수 항: `constant`
* 자기 스탯 항: `self_atk`, `self_sp`, `self_max_hp`, `self_hp`, `self_missing_hp`, `self_mp`, `self_max_mp`, `self_spd`, `self_spent_mp`
* 상대 스탯 항: `target_hp`, `target_max_hp`, `target_missing_hp`, `target_def`, `target_mr`, `target_spd`
* 구성:

  * `value = Σ(term.coef * term.value)`
* 예시(개념):

  * 어벤저(잃은 체력 비례): `self_missing_hp` 항 사용
  * 처형(적 결손 체력 비례): `target_missing_hp` 항 사용
  * 마나 버스트: `mp_cost = all_current_mp` + `self_spent_mp` 항 사용

### 3) 스페셜 태그(스킬/장비 공통)

* “필중”, “즉사”, “상태이상 면역 무시” 같은 것들은 `special_tags`로만 구현
* 태그는 별도 레지스트리에서 정의(추가/삭제 쉬움)

---

## V. 원소(속성) 시스템(전투 적용)

속성: 불/물/공기/흙/번개/아케인/무속성

* 상성/컨셉(요약):

  * 불: 물에 약 + 화상
  * 흙: 물에 약 + 강한 피해/광역 + 일부 패닉
  * 물: 피해 약 + 일부 빙결
  * 공기: 기본 피해 약 + 높은 계수 + 스피드형
  * 번개: 물에 강, 흙에 약 + 마비 + 저코스트 고효율(희귀)
  * 아케인: 약점 없음 + 고비용 고효율(최상위)
* 적용 방식:

  * “원소 상성 배수 테이블”을 config로 관리
  * 상태이상 부여/지속 보정도 챕터 룰/장비 룰로 확장 가능

---

## VI. 전투 외 이벤트(랜덤 인카운터)

* 랜덤 인카운터도 동일한 “이벤트-선택지-결과” 구조 
* 결과는:

  * 스탯 변화
  * 전투 발생
  * 상점/퀘스트 진입
  * 아이템/장비 획득 등

---

## VII. 상점/장비/인벤토리

### 1) 상점

* 소모품/장비 구매·판매
* 판매가는 상점가의 70% 
* 소모품 예:

  * HP/MP 포션
  * 연막탄(도주 보조)
  * 영구 스탯 증가 희귀 아이템 

### 2) 장비 슬롯(기획서 정리)

`기획gpt.txt`에서는 반지 2개 슬롯 , 과거 논의에서는 4개 슬롯도 있었음.
이건 **밸런스에 영향이 커서 “설정값”으로 처리**하는 게 안전하다.

* 기본안: 반지 슬롯 `RING_SLOTS = 2`
* 확장안: 반지 슬롯 `RING_SLOTS = 4` (대신 unique 제한/중첩 제한 필수)

나머지:

* 투구/흉갑/망토/각반/부츠
* 무기: 한손 2, 양손 1(양손이면 다른 무기 불가) 

### 3) 장비 특수효과/세트효과(필수)

* 장비 착용 시 스킬 부여(예: 특정 검 장착 → 스킬 해금)
* 원소 피해 감소, 상태이상 저항/면역 등
* 세트: n피스 착용 시 보너스 + 전투 중 버프 부여
* 구현은 전부 `special_tags`로 통일(장비도 스킬과 같은 태그 실행기 사용)

---

## VIII. 저장/불러오기

* 여러 슬롯 저장
* 저장 데이터(권장):

  * 플레이어 정보(레벨/경험치/스탯/포인트)
  * 현재 HP/MP/실드
  * 인벤토리(아이템/수량)
  * 장비 착용 상태
  * 습득/해금 스킬
  * 현재 챕터/이벤트 진행 플래그
* 도움말 텍스트는 사용자가 작성, 코드는 프레임만 제공 

---

## IX. 랭킹

* 등록 조건: 플레이어 사망 또는 클리어(챕터7) 
* 점수 공식(원문 유지):

  * 처치한 적 수(보스 5n, 엘리트 2n, 미니언 1n)
  * * 소지금
  * * 보유 아이템 가치
  * * 클리어 챕터 수(챕터당 10점, 최대 7챕터) 

---

## X. 챕터 구성(원문 보강 정리)

`기획gpt.txt`의 챕터 구성은 그대로 유지하되, 엔진 관점에서 필요한 “챕터 룰”을 명시적으로 분리해둔다. 

* 챕터 1: 서부 황야(레벨 1~8)

  * 튜토리얼 구간
  * 정예에 한해 마비/패닉 부여 등장
  * 중보스: 고블린 샤먼 / 보스: 고블린 챔피언 
* 챕터 2: 몰락한 왕국(8~20)

  * 마저 대비 소홀하면 피보는 구간
  * 중보스: 스켈레톤 드래곤 or 스톤 골렘 / 보스: 피의 영혼 
* 챕터 3: 숲의 양옥집(~30)

  * 챕터 룰: 회복계열 +10%
  * 중보스: 데스 나이트 / 보스: 뱀파이어 로드 
* 챕터 4: 전설의 유산(~55)

  * 아티팩트 파밍/빌드 확정(전직급 구간)
  * 중보스: 레드 드래곤 / 보스: 에인션트 드래곤 / 히든: 듀자 
* 챕터 5: 둠 이터널(~80)

  * 챕터 룰: 화속 피해 +10%, 수속 피해 +20%, 수속 MP +20%, 화상 지속 +2턴
  * 중보스: 헬 드래곤 / 보스: 사천왕 아스모데우스 
* 챕터 6: 이계의 침략자(~90)

  * 챕터 룰: MP +15%, 스킬 피해 +30%(양날의 검)
  * 중보스: 사천왕 아몬 / 보스: 크림즌 노바 트리니티 
* 챕터 7: 인마대전(~100)

  * 잡몹 없음(전부 보스급급)
  * 중보스: 듀란/콘빅트 / 보스: 마왕 앨리스 / 진최종: 슈미트
  * 슈미트는 “33% 깎고 필중 즉사기 연출로 마무리”라는 연출 규칙 명시 





# 0) 공통 규칙

## ID 규칙

* 모든 콘텐츠는 `id`로 참조한다. (표시 이름 `name`과 분리)
* `id`는 영문/숫자/언더스코어 권장: `goblin_shaman`, `skill_fireball` 등
* **중복 id 금지**, **참조 무결성 필수**(없는 id를 참조하면 로딩 실패)

## 수치 규칙

* `chance`: 0.0 ~ 1.0
* `duration_turns`: 1 이상 정수
* 스탯은 기본적으로 `>= 0` (예외: 디버프/패널티는 modifier에서 음수 허용)

## enum(문자열 상수) 목록

* `element`: `neutral | fire | water | air | earth | lightning | arcane`
* `damage_type`: `physical | magic | true`
* `skill_category`: `attack | heal | shield | buff | debuff | utility`
* `target`: `self | enemy | ally | all_enemies | all_allies | random_enemy | random_ally`
* `tier`: `minion | elite | boss | hidden_boss`
* `slot`: `helmet | chest | cloak | legs | boots | ring | weapon_1h | weapon_2h`

---

# 1) config.json (전역 룰)

## 목적

* 방어 공식, 원소 상성, 치명타 배율, 링 슬롯 수 등 **밸런스/룰 파라미터를 전부 코드 밖으로**.

## 구조(스펙)

```json
{
  "version": 1,
  "defense_model": {
    "type": "CURVE", 
    "k": 0.02,
    "min_mult": 0.2
  },
  "crit": { "base_chance": 0.05, "mult": 1.5 },
  "accuracy": { "min_hit": 0.05, "blind_mult": 0.7 },
  "shield": { "bypass_defense": true },
  "equipment_rules": { "ring_slots": 2 },
  "elements": {
    "multiplier": {
      "fire": { "water": 0.8, "earth": 1.0, "air": 1.0, "fire": 1.0, "lightning": 1.0, "arcane": 1.0, "neutral": 1.0 },
      "water": { "fire": 1.2, "earth": 1.2, "lightning": 0.9, "water": 1.0, "air": 1.0, "arcane": 1.0, "neutral": 1.0 },
      "earth": { "water": 0.8, "lightning": 1.2, "...": 1.0 },
      "lightning": { "water": 1.2, "earth": 0.8, "...": 1.0 },
      "air": { "...": 1.0 },
      "arcane": { "...": 1.0 },
      "neutral": { "...": 1.0 }
    }
  }
}
```

### 방어 모델 타입 제안

* `LOL`: `mult = 100/(100+def)` (스케일 큰 게임용)
* `CURVE`: `mult = max(min_mult, 1/(1 + def*k))` (스케일 작은 게임용 권장)
* `LINEAR_CAP`: `mult = clamp(1 - def*k, min_mult, 1)`

---

# 2) skills.json

## 목적

* 스킬은 “풀(pool)”이고, 실제 사용 가능 여부는 **개체가 배정받은 스킬 목록**으로 결정.

## 구조(스펙)

```json
{
  "version": 1,
  "skills": {
    "<skill_id>": {
      "name": "표시명",
      "description": "플레이버 텍스트",
      "element": "fire",
      "category": "attack",
      "target": "enemy",
      "mp_cost": 10,

      "components": [
        {
          "kind": "damage",
          "damage_type": "magic",
          "terms": [
            { "stat": "constant", "coef": 15 },
            { "stat": "self_spell_power", "coef": 0.8 }
          ]
        }
      ],

      "status_effects": [
        { "status": "burn", "target": "enemy", "chance": 0.3, "duration_turns": 3 }
      ],

      "special_tags": ["TAG_ALWAYS_HIT"]
    }
  }
}
```

## terms.stat 허용 목록(필수)

* `constant`
* 자기: `self_attack | self_spell_power | self_defense | self_magic_resist | self_speed | self_hp | self_max_hp | self_missing_hp | self_mp | self_max_mp | self_missing_mp | self_spent_mp`
* 상대: `target_hp | target_max_hp | target_missing_hp | target_mp | target_max_mp | target_missing_mp | target_defense | target_magic_resist | target_speed`

> 이렇게 해두면 “처형/어벤저/마나버스트” 같은 걸 특수공식 없이 구현 가능.

---

# 3) enemies.json

## 목적

* 적은 “현재 스탯”을 저장하지 않는다.
* **1레벨 기준 스탯 + 성장률**로 계산한다.

## 구조(스펙)

```json
{
  "version": 1,
  "enemies": [
    {
      "id": "goblin",
      "name": "고블린",
      "tier": "minion",
      "element": "neutral",
      "base_level": 1,

      "base_stats": {
        "max_hp": 20, "max_mp": 0,
        "attack": 2, "spell_power": 0,
        "defense": 0, "magic_resist": 0,
        "speed": 3, "crit_chance": 0.02
      },

      "growth_per_level": {
        "max_hp": 2.1, "max_mp": 0.2,
        "attack": 0.3, "spell_power": 0.0,
        "defense": 0.05, "magic_resist": 0.02,
        "speed": 0.03
      },

      "rewards": { "xp": 5, "gold_from_xp_ratio": 0.8 },

      "skill_assignment": {
        "base": ["skill_scratch"],
        "by_level": [
          { "min_level": 10, "add": ["skill_throw_stone"] },
          { "min_level": 20, "add": ["skill_panic_shout"] }
        ]
      },

      "status_resistance": {
        "panic": { "mult": 1.0 },
        "freeze": { "mult": 1.0 }
      }
    }
  ]
}
```

### status_resistance 규칙

* 기본값(미기재): `mult = 1.0`
* 보스 정책 예:

  * boss: `panic.mult = 0` (면역)
  * elite: `panic.mult = 0.5` (확률 반감)

---

# 4) consumables.json

## 목적

* 소모품 효과는 “타입 기반 effect 리스트”로 통일.
* 후반에도 쓸모 있게 **고정 + % 혼합**을 기본 전략으로.

## 구조(스펙)

```json
{
  "version": 1,
  "consumables": {
    "<item_id>": {
      "name": "소형 체력 포션",
      "description": "…",
      "rarity": "common",
      "price": 30,
      "sell_ratio": 0.7,
      "usable_in_battle": true,

      "effects": [
        { "type": "heal_hp", "flat": 30, "percent_max_hp": 0.2 }
      ],

      "special_tags": []
    }
  }
}
```

## effects.type 목록(권장 최소)

* `heal_hp` `{flat, percent_max_hp}`
* `heal_mp` `{flat, percent_max_mp}`
* `add_shield` `{flat, percent_max_hp}`
* `remove_status` `{statuses:[...]}`  // burn/poison/bleed/freeze/stun/panic/blind
* `apply_status` `{status, chance, duration_turns, target}`
* `apply_buff` `{duration_turns, modifiers:[{stat, flat, percent}] }`
* `revive` `{percent_max_hp}` (필요 시)
* `escape_bonus` `{flat}` 또는 `{mult}`
* `perm_stats` `{mods:[{stat, flat}]}` (희귀 영구강화)
* `cast_skill` `{skill_id}` (스크롤류)

---

# 5) equipment.json

## 목적

* 슬롯/스탯 보정/세트/스킬 부여/특수효과를 **전부 데이터화**
* 장비 고유효과는 `special_tags`로 처리

## 구조(스펙)

```json
{
  "version": 1,
  "equipment": {
    "<equip_id>": {
      "name": "플레임 소드",
      "description": "…",
      "rarity": "rare",
      "slot": "weapon_1h",
      "price": 220,
      "sell_ratio": 0.7,
      "unique": false,

      "stats": {
        "flat": { "attack": 6, "speed": 1, "max_hp": 0, "max_shield": 0 },
        "percent": { "attack": 0.0, "spell_power": 0.0 }
      },

      "granted_skills": ["skill_flame_blade"],
      "set_name": "dragon_set",
      "special_tags": ["TAG_ELEMENT_DAMAGE_BONUS_FIRE_10"]
    }
  },

  "sets": {
    "dragon_set": {
      "name": "드래곤 세트",
      "pieces": ["dragon_helmet", "dragon_chest", "dragon_legs", "dragon_boots"],
      "bonuses": [
        { "pieces": 2, "stats": { "percent": { "max_hp": 0.05 } }, "special_tags": [] },
        { "pieces": 4, "stats": { "percent": { "all": 0.05 } }, "special_tags": ["TAG_BUFF_DRAGON_POWER"] }
      ]
    }
  }
}
```

### 무기 슬롯 검증 룰(필수)

* `weapon_2h` 장착 시 `weapon_1h` 장착 불가
* `weapon_1h`는 최대 2개

### ring 슬롯

* `config.json.equipment_rules.ring_slots`로 제한(2 또는 4)

---

# 6) specials.json (스페셜 태그 레지스트리)

## 목적

* “스탯/terms만으로 구현이 어려운 특수효과”를 태그로 분리
* 실행 타이밍(hook) 명시 → 전투 파이프라인에 끼워넣기 쉬움

## 구조(스펙)

```json
{
  "version": 1,
  "specials": {
    "TAG_ALWAYS_HIT": {
      "description": "필중",
      "hooks": ["pre_accuracy"],
      "params": {}
    },
    "TAG_ELEMENT_DAMAGE_BONUS_FIRE_10": {
      "description": "화염 속성 피해 +10%",
      "hooks": ["pre_damage"],
      "params": { "element": "fire", "mult": 1.1 }
    },
    "TAG_BUFF_DRAGON_POWER": {
      "description": "전투 중 드래곤의 힘 버프 부여",
      "hooks": ["on_battle_start"],
      "params": { "duration_turns": 5, "mods": [{ "stat": "attack", "percent": 0.1 }] }
    }
  }
}
```

## hooks 표준(권장)

* 전투 흐름: `on_battle_start | pre_action | pre_accuracy | pre_damage | pre_shield | post_damage | post_action | on_kill | on_death | on_turn_end`
* 장비/패시브: `on_equip | on_unequip`

---

# 7) save.json (저장 데이터)

## 구조(스펙)

```json
{
  "version": 1,
  "player": {
    "name": "…",
    "level": 12,
    "xp": 340,
    "gold": 210,
    "base_stats": { "max_hp": 80, "max_mp": 30, "attack": 10, "spell_power": 6, "defense": 4, "magic_resist": 3, "speed": 6 },
    "current": { "hp": 70, "mp": 18, "shield": 0 },
    "stat_points": 2
  },
  "progress": { "chapter": 3, "flags": { "met_npc_x": true } },
  "inventory": {
    "consumables": { "hp_potion_s": 3, "antidote": 1 },
    "equipment": ["iron_sword", "leather_boots"]
  },
  "equipped": {
    "helmet": null, "chest": "leather_armor", "cloak": null, "legs": null, "boots": "leather_boots",
    "rings": ["ring_ruby", null],
    "weapons": ["iron_sword", null]
  },
  "learned_skills": ["skill_fireball", "skill_heal"]
}
```

---

# 8) 로더 검증 규칙 + 에러 메시지 규격(강제)

## 에러 포맷(권장)

* `E####: <file>:<path> - <message> (hint: ...)`

예시:

* `E1001: skills.json:skills.skill_fireball.mp_cost - must be >= 0`

## 필수 검증 목록

### (A) 공통

* `E1000` JSON 파싱 실패
* `E1001` 필수 필드 누락
* `E1002` 타입 불일치(숫자 대신 문자열 등)
* `E1003` enum 값 불일치
* `E1004` id 중복

### (B) 참조 무결성

* `E2001` enemies가 참조하는 `skill_id`가 skills에 없음
* `E2002` equipment.granted_skills가 skills에 없음
* `E2003` consumables.cast_skill.skill_id가 skills에 없음
* `E2004` sets.pieces의 equip_id가 equipment에 없음
* `E2005` special_tags가 specials에 없음

### (C) 수치 범위

* `E3001` chance가 0~1 범위 밖
* `E3002` duration_turns < 1
* `E3003` base_stats.max_hp <= 0
* `E3004` growth_per_level에 음수(허용 여부 옵션: 일반적으로 금지)

### (D) 스킬 수식

* `E4001` component.kind 미지원
* `E4002` damage_type 미지원
* `E4003` terms.stat 미지원
* `E4004` components가 비어있는데 category가 attack/heal/shield인 경우(효과 없음)

### (E) 장비 규칙

* `E5001` slot 미지원
* `E5002` set_name이 있는데 sets에 정의 없음
* `E5003` bonuses.pieces가 1..N 범위 밖
* (런타임 검증) `weapon_2h` + `weapon_1h` 동시 착용 시도 → 사용자 메시지 출력(에러코드 없어도 됨)

---

# 9) 최소 예시 세트(“로드 성공” 스모크 테스트용)

## skills.json (최소 1개)

```json
{
  "version": 1,
  "skills": {
    "skill_fireball": {
      "name": "파이어볼",
      "description": "불꽃 구체를 날린다.",
      "element": "fire",
      "category": "attack",
      "target": "enemy",
      "mp_cost": 8,
      "components": [
        { "kind": "damage", "damage_type": "magic",
          "terms": [ { "stat": "constant", "coef": 12 }, { "stat": "self_spell_power", "coef": 0.8 } ]
        }
      ],
      "status_effects": [ { "status": "burn", "target": "enemy", "chance": 0.25, "duration_turns": 3 } ],
      "special_tags": []
    }
  }
}
```

## enemies.json (최소 1개)

```json
{
  "version": 1,
  "enemies": [
    {
      "id": "goblin",
      "name": "고블린",
      "tier": "minion",
      "element": "neutral",
      "base_level": 1,
      "base_stats": { "max_hp": 20, "max_mp": 0, "attack": 2, "spell_power": 0, "defense": 0, "magic_resist": 0, "speed": 3 },
      "growth_per_level": { "max_hp": 2.0, "attack": 0.3, "defense": 0.05, "speed": 0.03 },
      "rewards": { "xp": 5, "gold_from_xp_ratio": 0.8 },
      "skill_assignment": { "base": [], "by_level": [] }
    }
  ]
}
```

## consumables.json (최소 1개)

```json
{
  "version": 1,
  "consumables": {
    "hp_potion_s": {
      "name": "소형 체력 포션",
      "description": "체력을 회복한다.",
      "rarity": "common",
      "price": 30,
      "sell_ratio": 0.7,
      "usable_in_battle": true,
      "effects": [ { "type": "heal_hp", "flat": 30, "percent_max_hp": 0.2 } ],
      "special_tags": []
    }
  }
}
```

## equipment.json (최소 1개)

```json
{
  "version": 1,
  "equipment": {
    "iron_sword": {
      "name": "철검",
      "description": "무난한 한손검.",
      "rarity": "common",
      "slot": "weapon_1h",
      "price": 100,
      "sell_ratio": 0.7,
      "unique": false,
      "stats": { "flat": { "attack": 3 }, "percent": {} },
      "granted_skills": [],
      "set_name": null,
      "special_tags": []
    }
  },
  "sets": {}
}
```

# Desia – 챕터 구성 기획서

## 0. 공통 구조 규칙

* 총 **7개 챕터**, 각 챕터는 **12 Act**로 구성
* **1 Act = 전투 / 상점 / 이벤트 중 1개**
* **Act 12는 항상 챕터 보스 전투**
* 레벨 구간은 *대략적 가이드*이며, 실제 밸런싱 과정에서 조정 가능

---

## 1. 세계관 배경 요약

외신과 괴물로 혼돈에 빠진 세계.
천제 **아이테르**의 승천 이후 질서는 회복되었으나, 마신의 권속과 마왕군은 여전히 잔존한다.

플레이어는 트리얼 왕국 소속 모험가로, 본래 목표는 "적당히 벌고 평생 놀고먹기"였으나,
세계의 흐름은 점점 플레이어를 중심으로 왜곡되기 시작한다.

---

## Chapter 1. 서부 황야 지대

### ■ 예상 레벨

* Lv 1 ~ 8

### ■ 챕터 컨셉

* 게임 전반 구조를 익히는 **튜토리얼 챕터**
* 기본 전투, 스탯 개념, 속성·저항의 기초 학습
* 상태이상은 **정예 몬스터 한정**으로 제한

### ■ 일반 몬스터 설계

* 유형 A: 낮은 공격력 + 독 / 화상 등 상태이상
* 유형 B: 높은 공격력 + 낮은 체력

**등장 몬스터**

* 고블린 / 좀비 / 스켈레톤 / 슬라임 / 망령 / 매드 프리스트

### ■ 보스 구성

* 중간 보스: **고블린 샤먼**

  * 소환형 + 상태이상 방해 중심
* 챕터 보스: **고블린 챔피언**

  * 고체력·고공격력 / 낮은 마법 저항

---

## Chapter 2. 몰락한 왕국

### ■ 예상 레벨

* Lv 8 ~ 20

### ■ 챕터 컨셉

* 본격적인 빌드 체크 구간
* **마법 저항 대비 여부**가 난이도를 크게 좌우

### ■ 등장 몬스터

* 타락한 마도사 / 매드 프리스트 / 도적 / 나이트워커
* 망령 / 스켈레톤 전사 / 변질된 엘리멘터리

### ■ 보스 구성

* 중간 보스: **스켈레톤 드래곤** 또는 **스톤 골렘**

  * 고방어·고마저 탱커형
* 챕터 보스: **피의 영혼**

  * 고체력 + 고주문력 지속 압박형

---

## Chapter 3. 숲의 양옥집

### ■ 예상 레벨

* Lv 20 ~ 30

### ■ 챕터 규칙

* 회복 계열 스킬 회복량 **+10%**

### ■ 챕터 컨셉

* 성장 가속 구간
* 흡혈·회복·지속전 메커니즘 집중

### ■ 등장 몬스터

* 흡혈귀 / 뱀파이어
* 강화·거대 슬라임
* 고레벨 변질 엘리멘터리
* 매드 프리스트 / 타락한 마도사

### ■ 보스 구성

* 중간 보스: **데스 나이트**
* 챕터 보스: **뱀파이어 로드**

---

## Chapter 4. 전설의 유산

### ■ 예상 레벨

* Lv 30 ~ 55

### ■ 챕터 컨셉

* 다음 고난도 구간을 대비하는 **빌드 확정 챕터**
* 전설 장비 + 신화 아티팩트 획득
* 사실상의 **전직 시스템 역할**

### ■ 등장 몬스터

* 수호 영룡 / 드래곤 시리즈
* 가디언 슬라임 / 갓 슬라임 (엘리트)
* 고레벨 슬라임 / 나이트워커 / 와이번
* 스톤 골렘 / 나가

### ■ 보스 구성

* 중간 보스: **레드 드래곤**
* 챕터 보스: **에인션트 드래곤**
* 히든 보스: **이차원 파수기 듀자**

  * 챕터 6급 몬스터를 조기 배치한 파워 인플레이션 연출

---

## Chapter 5. 둠 이터널

### ■ 예상 레벨

* Lv 55 ~ 80

### ■ 챕터 규칙

* 화속성 피해 +10%
* 수속성 피해 +20%
* 수속성 스킬 MP 소모 +20%
* 화상 지속시간 +2턴

### ■ 챕터 컨셉

* 최고 성장률 + 최고 난이도 상승 구간
* **화상 중심 지옥 메타**

### ■ 등장 몬스터

* 지옥의 변견 / 케르베로스
* 고레벨 화염 정령 / 망령 / 스켈레톤
* 피의 영혼 / 이차원정령 비잠

### ■ 보스 구성

* 중간 보스: **헬 드래곤**
* 챕터 보스: **사천왕 아스모데우스**

---

## Chapter 6. 이계의 침략자

### ■ 예상 레벨

* Lv 80 ~ 90

### ■ 챕터 규칙

* 마나 소모 +15%
* 스킬 피해량 +30%

### ■ 챕터 컨셉

* 양날의 검 챕터
* 빠른 승부 = 빠른 사망
* 대부분의 적이 상태이상 보유 (특히 패닉)

### ■ 등장 몬스터

* 이차원 시리즈
* 암흑차원신 **크림즌 노바**

### ■ 보스 구성

* 중간 보스: **사천왕 아몬**
* 챕터 보스: **크림즌 노바 트리니티**

  * 물리·마법·복합 전천후 보스

---

## Chapter 7. 인마대전

### ■ 예상 레벨

* Lv 90 ~ 100

### ■ 챕터 컨셉

* 최종장
* 잡몹 없음, 전원 보스급 개체

### ■ 등장 몬스터

* 종말의 선구자 / 핏빛 갈퀴
* 플레시 콜로서스 / 피의 영혼
* 이차원수 다크 가넥스 / 이차원제 게이라 가일
* 고레벨 뱀파이어 / 스켈레톤 / 타락한 마도사

### ■ 보스 구성

* 중간 보스: **사천왕 듀란**, **사천왕 콘빅트**
* 챕터 보스: **마왕 앨리스**
* 진 최종 보스: **슈미트**

  * 체력 33% 이후 즉사 연출기 *저스티스 오브 아이테르*로 클리어

