import Enum from "@/constants/Enum";

const SearchTypeEnum = {
	ALL: new Enum(0, 'ALL'),
	METADATA: new Enum(1, 'METADATA'),
	CONTENT: new Enum(2, 'CONTENT'),
};

export default SearchTypeEnum;