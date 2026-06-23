import re

with open('src/main/resources/templates/jobs/job-sheet-pdf.html', 'r', encoding='utf-8') as f:
    content = f.read()

# Find the right-grid-table
table_start = content.find('<table class="right-grid-table">')
table_end = content.find('</table>', table_start) + 8
table_html = content[table_start:table_end]

# 1. Update colgroup
new_colgroup = '''<colgroup>
                        <col style="width: 25%;" />
                        <col style="width: 15px;" />
                        <col style="width: 25%;" />
                        <col style="width: 15px;" />
                        <col style="width: 25%;" />
                        <col style="width: 25%;" />
                    </colgroup>'''
table_html = re.sub(r'<colgroup>.*?</colgroup>', new_colgroup, table_html, flags=re.DOTALL)

# 2. Add spacer before the last column of each row in the right-grid-table
def insert_spacer(match):
    row_inner = match.group(1)
    # The columns are <td> or <th> or whatever. We find all <td>s
    tds = re.findall(r'<td.*?>.*?</td>', row_inner, flags=re.DOTALL)
    if len(tds) == 5:
        # insert a spacer before the last td
        tds.insert(4, '<td class="spacer-cell">&#160;</td>')
        return '<tr>\n                        ' + '\n                        '.join(tds) + '\n                    </tr>'
    return match.group(0)

new_table_html = re.sub(r'<tr>(.*?)</tr>', insert_spacer, table_html, flags=re.DOTALL)

new_content = content[:table_start] + new_table_html + content[table_end:]

with open('src/main/resources/templates/jobs/job-sheet-pdf.html', 'w', encoding='utf-8') as f:
    f.write(new_content)

print('Done')
