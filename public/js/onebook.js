/*** 本検索 ***/
/* カルーセル　左右矢印 */
$(function(){
  $('.book_carousel').slick({
        dots: true,
        infinite: false,
        arrows: true,
        slidesToShow: 5,
        slidesToScroll: 5,
        prevArrow: '<button type="button" class="slick-prev btn btn-default"><span class="glyphicon glyphicon-chevron-left" aria-hidden="true"></span></button>',
      	nextArrow: '<button type="button" class="slick-next btn btn-default"><span class="glyphicon glyphicon-chevron-right" aria-hidden="true"></span></button>',
      	responsive: [{
            breakpoint: 768,
              settings: {
                slidesToShow: 3,
                slidesToScroll: 3,
            }
          },{
            breakpoint: 480,
              settings: {
                slidesToShow: 2,
                slidesToScroll: 2,
              }
            }
          ]
     });
});

/* グリッド */
$(function(){
    $('.book_grid').masonry({
        itemSelector: '.book_sheet',
        columnWidth: 160
    });
});



/*** 本情報 ***/
$(function() {
	/* 修正ボタン押下 */
	$('.gp.b_edit').click(function() {
		$('.gp.text, .gp.b_edit').hide();
		$('.gp.Form, .gp.b_cancel, .gp.b_submit').show();
		return false;
	});
	/* キャンセルボタン押下 */
	$('.gp.b_cancel').click(function() {
		$('.gp.Form, .gp.b_cancel, .gp.b_submit').hide();
		$('.gp.text, .gp.b_edit').show();
		return false;
	});
})

/*** 本コメント ***/
$(function() {
	/* 修正ボタン押下 */
	$('.comment.b_edit').click(function() {
		$('.myself.read, .myself.memo, .comment.b_edit').hide();
		$('.myself.readForm, .myself.memoForm, .myself.calendarForm, .comment.b_cancel, .comment.b_submit').show();
		return false;
	});
	/* キャンセルボタン押下 */
	$('.comment.b_cancel').click(function() {
		$('.myself.readForm, .myself.memoForm, .myself.calendarForm, .comment.b_cancel, .comment.b_submit').hide();
		$('.myself.read, .myself.memo, .comment.b_edit').show();
		return false;
	});
	/* 読了チェック　セレクトボックス変更 */
	var startDateStr = ["開始予定日", "開始日", "開始日"];
	var endDateStr = ["", "終了予定日", "終了日"];

	$('.readForm > select').change(function() {
			var readStartDate = $('#readStartDate').val();
			var readEndDate = $('#readEndDate').val();
			var readFlag = $(this).val();
			var dateFormText = 
				'<label>' + startDateStr[readFlag] + '</label>' +
		        '<input id="readStartDate" name="start_day" type="date" value="' + readStartDate + '" class="form-control">';
		    if (readFlag == 0) {
		    	dateFormText += '<input id="readEndDate" name="finish_day" type="hidden" value="' + readEndDate + '">';
		    } else {
		    	dateFormText += 
		    		'<label>' + endDateStr[readFlag] + '</label>' +
		    		'<input id="readEndDate" type="date" name="finish_day" value="' + readEndDate + '" class="form-control">';
		    }
		    $('.myself.calendarForm').html(dateFormText);
	})

})


/*** 本棚管理 ***/
/* 場所削除時確認 */
/*$(function(){
	$("delPlace").click( function() { */
function delPlace() {
	var ret = false;
		swal({
			title : "本当に削除していいですか？",
			text : "削除された場所に登録されていた本の場所は、すべて「未分類」に変更されます",
			type : "warning",
			showCancelButton : true,
			confirmButtonColor : "#DD6B55",
			confirmButtonText : "削除する",
			cancelButtonText : "キャンセル",
			closeOnConfirm : true,
			closeOnCancel : true
		}, function(isConfirm) {
			if(isConfirm) {
				ret = true;
				//swal("削除しました！", "", "success");
				$("#delPlace").submit();
			} else {
				//swal("Cancelled", "Your imaginary file is safe :)", "error");
			}
		});
		return ret;
};			
/*	});
}); */ 
/* ジャンル削除時確認 */
function delGenre() {
	var ret = false;
		swal({
			title : "本当に削除していいですか？",
			text : "削除されたジャンルに登録されていた本のジャンルは、すべて「未分類」に変更されます",
			type : "warning",
			showCancelButton : true,
			confirmButtonColor : "#DD6B55",
			confirmButtonText : "削除する",
			cancelButtonText : "キャンセル",
			closeOnConfirm : true,
			closeOnCancel : true
		}, function(isConfirm) {
			if(isConfirm) {
				ret = true;
				//swal("削除しました！", "", "success");
				$("#delGenre").submit();
			} else {
				//swal("Cancelled", "Your imaginary file is safe :)", "error");
			}
		});
		return ret;
};

/*** 本棚へ本登録（複数） ***/
$(function() {
    /* ファイルアップロードボタン 表示 */
    $('#file-input').change(function() {
      $('#cover').html($(this).val());
    });

    /* ファイルアップロードボタン クリック */
    $('.file_open').click(function() {
        $('#file-input').click();
    });

    /* アイテム削除ボタン */
    $('.item_remove').click(function() {
        alert('この本の登録をやめますか？');
    });

    $('#register-books').selectMultiple();
    $('#register-books').selectMultiple('select_all');    
});