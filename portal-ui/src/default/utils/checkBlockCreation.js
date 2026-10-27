const canBlockBeCreated = (hierarchy, createdBlock) => {
	for (const element of hierarchy) {
		if (element.blockId !== undefined) {
			if (doBlocksCut(element, createdBlock)) {
				return false;
			}

			if (!canBlockBeCreated(element.children, createdBlock)) {
				return false;
			}
		}
	}
	return true;
};

const doBlocksCut = (block1, block2) => {
	if (block1.firstVerseNo == block2.firstVerseNo ||
		block1.lastVerseNo == block2.lastVerseNo) {
		return false;
	}

	if(block1.firstVerseNo == block2.lastVerseNo ||
		block1.lastVerseNo == block2.firstVerseNo) {
		return true;
	}

	const firstIn = block1.firstVerseNo > block2.firstVerseNo && block1.firstVerseNo < block2.lastVerseNo;
	const lastIn = block1.lastVerseNo > block2.firstVerseNo && block1.lastVerseNo < block2.lastVerseNo;

	return firstIn != lastIn;
};

export default canBlockBeCreated;