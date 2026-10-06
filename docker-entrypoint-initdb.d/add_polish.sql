        CREATE TEXT SEARCH DICTIONARY polish_ispell (
          Template = ispell,
          DictFile = polish,
          AffFile = polish,
          StopWords = polish
        );

        CREATE TEXT SEARCH CONFIGURATION polish(parser = default);

        ALTER TEXT SEARCH CONFIGURATION polish
          ALTER MAPPING FOR asciiword, asciihword, hword_asciipart, word, hword, hword_part
          WITH polish_ispell, simple;
          
	ALTER TEXT SEARCH CONFIGURATION polish
          ALTER MAPPING FOR numword, numhword, hword_numpart, email, host, url, sfloat, float, int, uint, version, url_path, file
          WITH simple;
