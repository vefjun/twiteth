(ns twiteth.constants
  (:require [clojure.tools.reader :as reader]))

(def mist? (boolean (aget js/window "mist")))
(def contracts-version "1.0.17")

(def query-parsers
  {:search/category js/parseInt})

(def keyword->query
  {:search/category "cat"})

(def currencies-backward-comp
  {:eth 0
   :usd 1
   :eur 2})

(def currencies
  {0 "Ξ"
   1 "$"
   2 "€"
   3 "£"
   4 "\u20BD"
   5 "¥"
   6 "¥"})

(def currency-code->id
  {:ETH 0
   :USD 1
   :EUR 2
   :GBP 3
   :RUB 4
   :CNY 5
   :JPY 6})

(def currency-id->code
  {0 :ETH
   1 :USD
   2 :EUR
   3 :GBP
   4 :RUB
   5 :CNY
   6 :JPY})


(def united-states
  ["Alabama"
   "Alaska"
   "Arizona"
   "Arkansas"
   "California"
   "Colorado"
   "Connecticut"
   "Delaware"
   "District Of Columbia"
   "Florida"
   "Georgia"
   "Hawaii"
   "Idaho"
   "Illinois"
   "Indiana"
   "Iowa"
   "Kansas"
   "Kentucky"
   "Louisiana"
   "Maine"
   "Maryland"
   "Massachusetts"
   "Michigan"
   "Minnesota"
   "Mississippi"
   "Missouri"
   "Montana"
   "Nebraska"
   "Nevada"
   "New Hampshire"
   "New Jersey"
   "New Mexico"
   "New York"
   "North Carolina"
   "North Dakota"
   "Ohio"
   "Oklahoma"
   "Oregon"
   "Pennsylvania"
   "Rhode Island"
   "South Carolina"
   "South Dakota"
   "Tennessee"
   "Texas"
   "Utah"
   "Vermont"
   "Virginia"
   "Washington"
   "West Virginia"
   "Wisconsin"
   "Wyoming"])

(def countries
  ["Afghanistan"
   "Åland Islands"
   "Albania"
   "Algeria"
   "American Samoa"
   "Andorra"
   "Angola"
   "Anguilla"
   "Antarctica"
   "Antigua and Barbuda"
   "Argentina"
   "Armenia"
   "Aruba"
   "Australia"
   "Austria"
   "Azerbaijan"
   "Bahamas"
   "Bahrain"
   "Bangladesh"
   "Barbados"
   "Belarus"
   "Belgium"
   "Belize"
   "Benin"
   "Bermuda"
   "Bhutan"
   "Bolivia"
   "Bosnia and Herzegovina"
   "Botswana"
   "Bouvet Island"
   "Brazil"
   "British Indian Ocean Territory"
   "Brunei Darussalam"
   "Bulgaria"
   "Burkina Faso"
   "Burundi"
   "Cambodia"
   "Cameroon"
   "Canada"
   "Cape Verde"
   "Cayman Islands"
   "Central African Republic"
   "Chad"
   "Chile"
   "China"
   "Christmas Island"
   "Cocos (Keeling) Islands"
   "Colombia"
   "Comoros"
   "Congo"
   "Congo, The Democratic Republic of The"
   "Cook Islands"
   "Costa Rica"
   "Cote D'ivoire"
   "Croatia"
   "Cuba"
   "Cyprus"
   "Czech Republic"
   "Denmark"
   "Djibouti"
   "Dominica"
   "Dominican Republic"
   "Ecuador"
   "Egypt"
   "El Salvador"
   "Equatorial Guinea"
   "Eritrea"
   "Estonia"
   "Ethiopia"
   "Falkland Islands (Malvinas)"
   "Faroe Islands"
   "Fiji"
   "Finland"
   "France"
   "French Guiana"
   "French Polynesia"
   "French Southern Territories"
   "Gabon"
   "Gambia"
   "Georgia"
   "Germany"
   "Ghana"
   "Gibraltar"
   "Greece"
   "Greenland"
   "Grenada"
   "Guadeloupe"
   "Guam"
   "Guatemala"
   "Guernsey"
   "Guinea"
   "Guinea-bissau"
   "Guyana"
   "Haiti"
   "Heard Island and Mcdonald Islands"
   "Holy See (Vatican City State)"
   "Honduras"
   "Hong Kong"
   "Hungary"
   "Iceland"
   "India"
   "Indonesia"
   "Iran, Islamic Republic of"
   "Iraq"
   "Ireland"
   "Isle of Man"
   "Israel"
   "Italy"
   "Jamaica"
   "Japan"
   "Jersey"
   "Jordan"
   "Kazakhstan"
   "Kenya"
   "Kiribati"
   "Korea, Democratic People's Republic of"
   "Korea, Republic of"
   "Kuwait"
   "Kyrgyzstan"
   "Lao People's Democratic Republic"
   "Latvia"
   "Lebanon"
   "Lesotho"
   "Liberia"
   "Liberland"
   "Libyan Arab Jamahiriya"
   "Liechtenstein"
   "Lithuania"
   "Luxembourg"
   "Macao"
   "Macedonia, The Former Yugoslav Republic of"
   "Madagascar"
   "Malawi"
   "Malaysia"
   "Maldives"
   "Mali"
   "Malta"
   "Marshall Islands"
   "Martinique"
   "Mauritania"
   "Mauritius"
   "Mayotte"
   "Mexico"
   "Micronesia, Federated States of"
   "Moldova, Republic of"
   "Monaco"
   "Mongolia"
   "Montenegro"
   "Montserrat"
   "Morocco"
   "Mozambique"
   "Myanmar"
   "Namibia"
   "Nauru"
   "Nepal"
   "Netherlands"
   "Netherlands Antilles"
   "New Caledonia"
   "New Zealand"
   "Nicaragua"
   "Niger"
   "Nigeria"
   "Niue"
   "Norfolk Island"
   "Northern Mariana Islands"
   "Norway"
   "Oman"
   "Pakistan"
   "Palau"
   "Palestinian Territory, Occupied"
   "Panama"
   "Papua New Guinea"
   "Paraguay"
   "Peru"
   "Philippines"
   "Pitcairn"
   "Poland"
   "Portugal"
   "Puerto Rico"
   "Qatar"
   "Reunion"
   "Romania"
   "Russian Federation"
   "Rwanda"
   "Saint Helena"
   "Saint Kitts and Nevis"
   "Saint Lucia"
   "Saint Pierre and Miquelon"
   "Saint Vincent and The Grenadines"
   "Samoa"
   "San Marino"
   "Sao Tome and Principe"
   "Saudi Arabia"
   "Senegal"
   "Serbia"
   "Seychelles"
   "Sierra Leone"
   "Singapore"
   "Slovakia"
   "Slovenia"
   "Solomon Islands"
   "Somalia"
   "South Africa"
   "South Georgia and The South Sandwich Islands"
   "Spain"
   "Sri Lanka"
   "Sudan"
   "Suriname"
   "Svalbard and Jan Mayen"
   "Swaziland"
   "Sweden"
   "Switzerland"
   "Syrian Arab Republic"
   "Taiwan"
   "Tajikistan"
   "Tanzania, United Republic of"
   "Thailand"
   "Timor-leste"
   "Togo"
   "Tokelau"
   "Tonga"
   "Trinidad and Tobago"
   "Tunisia"
   "Turkey"
   "Turkmenistan"
   "Turks and Caicos Islands"
   "Tuvalu"
   "Uganda"
   "Ukraine"
   "United Arab Emirates"
   "United Kingdom"
   "United States"
   "United States Minor Outlying Islands"
   "Uruguay"
   "Uzbekistan"
   "Vanuatu"
   "Venezuela"
   "Viet Nam"
   "Virgin Islands, British"
   "Virgin Islands, U.S."
   "Wallis and Futuna"
   "Western Sahara"
   "Yemen"
   "Zambia"
   "Zimbabwe"
   "not specified"])

(def languages
  ["аҧсуа бызшәа, аҧсшәа"
   "Afaraf"
   "Afrikaans"
   "Akan"
   "Shqip"
   "አማርኛ"
   "العربية"
   "aragonés"
   "Հայերեն"
   "অসমীয়া"
   "авар мацӀ, магӀарул мацӀ"
   "avesta"
   "aymar aru"
   "azərbaycan dili"
   "bamanankan"
   "башҡорт теле"
   "euskara, euskera"
   "беларуская мова"
   "বাংলা"
   "भोजपुरी"
   "Bislama"
   "bosanski jezik"
   "brezhoneg"
   "български език"
   "ဗမာစာ"
   "català"
   "Chamoru"
   "нохчийн мотт"
   "chiCheŵa, chinyanja"
   "中文 (Zhōngwén), 汉语, 漢語"
   "чӑваш чӗлхи"
   "Kernewek"
   "corsu, lingua corsa"
   "ᓀᐦᐃᔭᐍᐏᐣ"
   "hrvatski jezik"
   "čeština, český jazyk"
   "dansk"
   "Nederlands, Vlaams"
   "རྫོང་ཁ"
   "English"
   "Esperanto"
   "eesti, eesti keel"
   "Eʋegbe"
   "føroyskt"
   "vosa Vakaviti"
   "suomi, suomen kieli"
   "français, langue française"
   "Fulfulde, Pulaar, Pular"
   "galego"
   "ქართული"
   "Deutsch"
   "ελληνικά"
   "Avañe'ẽ"
   "ગુજરાતી"
   "Kreyòl ayisyen"
   "(Hausa) هَوُسَ"
   "עברית"
   "Otjiherero"
   "हिन्दी, हिंदी"
   "Hiri Motu"
   "magyar"
   "Interlingua"
   "Bahasa Indonesia"
   "Interlingue"
   "Gaeilge"
   "Asụsụ Igbo"
   "Iñupiaq, Iñupiatun"
   "Ido"
   "Íslenska"
   "italiano"
   "ᐃᓄᒃᑎᑐᑦ"
   "日本語 (にほんご)"
   "Basa Jawa"
   "kalaallisut, kalaallit oqaasii"
   "ಕನ್ನಡ"
   "Kanuri"
   "कश्मीरी, كشميري‎"
   "қазақ тілі"
   "ខ្មែរ, ខេមរភាសា, ភាសាខ្មែរ"
   "Gĩkũyũ"
   "Ikinyarwanda"
   "Кыргызча, Кыргыз тили"
   "коми кыв"
   "Kikongo"
   "한국어"
   "Kurdî, كوردی‎"
   "Kuanyama"
   "latine, lingua latina"
   "Lëtzebuergesch"
   "Luganda"
   "Limburgs"
   "Lingála"
   "ພາສາລາວ"
   "lietuvių kalba"
   "Tshiluba"
   "latviešu valoda"
   "Gaelg, Gailck"
   "македонски јазик"
   "fiteny malagasy"
   "bahasa Melayu, بهاس ملايو‎"
   "മലയാളം"
   "Malti"
   "te reo Māori"
   "मराठी"
   "Kajin M̧ajeļ"
   "Монгол хэл"
   "Dorerin Naoero"
   "Diné bizaad"
   "नेपाली"
   "Owambo"
   "Norsk bokmål"
   "Norsk nynorsk"
   "Norsk"
   "ꆈꌠ꒿ Nuosuhxop"
   "isiNdebele"
   "occitan, lenga d'òc"
   "ᐊᓂᔑᓈᐯᒧᐎᓐ"
   "ѩзыкъ словѣньскъ"
   "Afaan Oromoo"
   "ଓଡ଼ିଆ"
   "ирон æвзаг"
   "ਪੰਜਾਬੀ"
   "पाऴि"
   "فارسی"
   "język polski, polszczyzna"
   "پښتو"
   "Português"
   "Runa Simi, Kichwa"
   "rumantsch grischun"
   "Ikirundi"
   "Română"
   "Русский"
   "संस्कृतम्"
   "sardu"
   "सिन्धी, سنڌي، سندھی‎"
   "Davvisámegiella"
   "gagana fa'a Samoa"
   "yângâ tî sängö"
   "српски језик"
   "Gàidhlig"
   "chiShona"
   "සිංහල"
   "slovenčina, slovenský jazyk"
   "slovenski jezik, slovenščina"
   "Soomaaliga, af Soomaali"
   "Sesotho"
   "español"
   "Basa Sunda"
   "Kiswahili"
   "SiSwati"
   "svenska"
   "தமிழ்"
   "తెలుగు"
   "тоҷикӣ, toçikī, تاجیکی‎"
   "ไทย"
   "ትግርኛ"
   "བོད་ཡིག"
   "Türkmen, Түркмен"
   "Wikang Tagalog"
   "Setswana"
   "faka Tonga"
   "Türkçe"
   "Xitsonga"
   "татар теле, tatar tele"
   "Twi"
   "Reo Tahiti"
   "ئۇيغۇرچە ‎Uyghurche"
   "Українська"
   "اردو"
   "Oʻzbek, Ўзбек, أۇزبېك‎"
   "Tshivenḓa"
   "Tiếng Việt"
   "Volapük"
   "walon"
   "Cymraeg"
   "Wollof"
   "Frysk"
   "isiXhosa"
   "ייִדיש"
   "Yorùbá"
   "Saɯ cueŋƅ, Saw cuengh"
   "isiZulu"])