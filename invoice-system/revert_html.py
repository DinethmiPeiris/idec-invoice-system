import re

with open('src/main/resources/templates/jobs/job-sheet-pdf.html', 'r', encoding='utf-8') as f:
    content = f.read()

# Find the right-grid-table
table_start = content.find('<table class="right-grid-table">')
table_end = content.find('</table>', table_start) + 8
table_html = content[table_start:table_end]

# 1. Update colgroup back to 5 columns
old_colgroup = '''<colgroup>
                        <col style="width: 25%;" />
                        <col style="width: 15px;" />
                        <col style="width: 25%;" />
                        <col style="width: 25%;" />
                        <col style="width: 25%;" />
                    </colgroup>'''
table_html = re.sub(r'<colgroup>.*?</colgroup>', old_colgroup, table_html, flags=re.DOTALL)

# 2. Remove the spacer before the last column of each row in the right-grid-table
def remove_spacer(match):
    row_inner = match.group(1)
    tds = re.findall(r'<td.*?>.*?</td>', row_inner, flags=re.DOTALL)
    if len(tds) == 6:
        # remove the 5th element (index 4)
        tds.pop(4)
        return '<tr>\n                        ' + '\n                        '.join(tds) + '\n                    </tr>'
    return match.group(0)

new_table_html = re.sub(r'<tr>(.*?)</tr>', remove_spacer, table_html, flags=re.DOTALL)

new_content = content[:table_start] + new_table_html + content[table_end:]

with open('src/main/resources/templates/jobs/job-sheet-pdf.html', 'w', encoding='utf-8') as f:
    f.write(new_content)

print('Reverted')
